package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.CargaDiferenca;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.NotaDetalhe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tira dos totais as cargas que ficaram vazias porque TODAS as notas foram para outras cargas (reentrega ou
 * transferência), confirmado pelo log do WinThor. Só mexe em carga SINCRONIZADA (ninguém iniciou a viagem) já
 * marcada como "não encontrada no WinThor". O valor e o peso que ela tinha ficam guardados, junto com para onde
 * as notas foram, e a carga volta ao normal se o WinThor devolver nota para ela. Ação explícita do administrador:
 * nunca roda sozinha.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZerarCargasTransferidasService {

    private final ConferenciaTransportadoService conferencia;
    private final NamedParameterJdbcTemplate jdbc;

    public record CargaZerada(Integer numCar, BigDecimal valorAntes, String transferidaPara) {
    }

    /** O que seria zerado, sem mudar nada. */
    public List<CargaZerada> candidatas(YearMonth mes) {
        List<CargaZerada> r = new ArrayList<>();
        for (CargaDiferenca d : conferencia.gerar(mes, true).cargasComDiferenca()) {
            if (d.podeSerZerada()) {
                r.add(new CargaZerada(d.numCar(), d.valorFrotaPro(), destinos(d)));
            }
        }
        return r;
    }

    @Caching(evict = {
            @CacheEvict(value = "carga_listar", allEntries = true),
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_data_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_saida", allEntries = true),
            @CacheEvict(value = "carga_periodo_criacao", allEntries = true),
            @CacheEvict(value = "carga_motorista", allEntries = true),
            @CacheEvict(value = "carga_caminhao", allEntries = true),
            @CacheEvict(value = "carga_minha_atual", allEntries = true)
    })
    @Transactional
    public List<CargaZerada> aplicar(YearMonth mes) {
        List<CargaZerada> aplicadas = new ArrayList<>();
        for (CargaZerada c : candidatas(mes)) {
            int linhas = jdbc.update("""
                    UPDATE tb_carga
                       SET valor_antes_transferencia = valor_total,
                           peso_antes_transferencia  = peso_carga,
                           transferida_para          = :para,
                           valor_total               = 0,
                           peso_carga                = 0
                     WHERE numero_carga_externo = :numero
                       AND status_carga = 'SINCRONIZADA'
                       AND nao_encontrada_no_winthor = true
                       AND transferida_para IS NULL
                    """, new MapSqlParameterSource()
                    .addValue("para", c.transferidaPara())
                    .addValue("numero", String.valueOf(c.numCar())));
            if (linhas > 0) {
                aplicadas.add(c);
                log.info("Carga {} zerada dos totais: valor {} foi para {}", c.numCar(), c.valorAntes(), c.transferidaPara());
            }
        }
        conferencia.invalidar(mes);
        return aplicadas;
    }

    private static String destinos(CargaDiferenca d) {
        String texto = d.notas().stream()
                .map(NotaDetalhe::paraCarga)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return texto.length() > 300 ? texto.substring(0, 297) + "..." : texto;
    }
}
