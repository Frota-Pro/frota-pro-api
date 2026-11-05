package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.CargaFp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Lê, só leitura, as cargas do FrotaPRO (com as notas de cada uma) que entram na conferência: as datadas no
 * período e as dos carregamentos citados pelo WinThor, mesmo datadas em outro mês.
 */
@Component
@RequiredArgsConstructor
public class CargasFrotaProLeitor {

    private final NamedParameterJdbcTemplate jdbc;

    public List<CargaFp> buscar(LocalDate inicio, LocalDate fim, Collection<Integer> numerosDeCarregamento) {
        List<String> numeros = numerosDeCarregamento.stream().map(String::valueOf).toList();

        var params = new MapSqlParameterSource()
                .addValue("inicio", inicio)
                .addValue("fim", fim)
                // lista vazia viraria IN () e o Postgres recusa
                .addValue("numeros", numeros.isEmpty() ? List.of("-") : numeros);

        List<Linha> linhas = jdbc.query("""
                SELECT c.id, c.numero_carga_externo, c.data_faturamento, c.valor_total, c.status_carga,
                       c.nao_encontrada_no_winthor, c.diminuicao_peso_valor_bloqueada,
                       c.peso_carga, c.origem_carga, c.origem_carga_numero, c.valor_winthor, c.transferida_para,
                       m.nome AS motorista, m.externo AS motorista_externo, k.placa AS placa
                  FROM tb_carga c
                  LEFT JOIN tb_motorista m ON m.id = c.motorista_id
                  LEFT JOIN tb_caminhao k ON k.id = c.caminhao_id
                 WHERE c.numero_carga_externo IS NOT NULL
                   AND (c.data_faturamento BETWEEN :inicio AND :fim OR c.numero_carga_externo IN (:numeros))
                """, params, (rs, i) -> new Linha(
                rs.getObject("id", UUID.class),
                numeroOuNulo(rs.getString("numero_carga_externo")),
                rs.getDate("data_faturamento") != null ? rs.getDate("data_faturamento").toLocalDate() : null,
                rs.getBigDecimal("valor_total"),
                rs.getString("status_carga"),
                rs.getBoolean("nao_encontrada_no_winthor"),
                rs.getBoolean("diminuicao_peso_valor_bloqueada"),
                // Carga.pesoCarga é guardado em TONELADAS; a conferência trabalha em kg
                rs.getBigDecimal("peso_carga") == null ? null : rs.getBigDecimal("peso_carga").multiply(java.math.BigDecimal.valueOf(1000)),
                rs.getString("origem_carga"),
                rs.getString("origem_carga_numero"),
                rs.getBigDecimal("valor_winthor"),
                rs.getString("transferida_para") != null,
                rs.getString("motorista"),
                rs.getBoolean("motorista_externo"),
                rs.getString("placa")));

        Map<UUID, Set<Long>> notasPorCarga = new HashMap<>();
        if (!linhas.isEmpty()) {
            List<UUID> ids = linhas.stream().map(Linha::id).toList();
            jdbc.query("SELECT n.carga_id, n.nota FROM tb_carga_nota n WHERE n.carga_id IN (:ids)",
                    new MapSqlParameterSource("ids", ids), rs -> {
                        Long numero = numeroLongOuNulo(rs.getString("nota"));
                        if (numero != null) {
                            notasPorCarga.computeIfAbsent(rs.getObject("carga_id", UUID.class), k -> new HashSet<>()).add(numero);
                        }
                    });
        }

        List<CargaFp> resultado = new ArrayList<>();
        for (Linha l : linhas) {
            if (l.numCar() == null) continue;
            resultado.add(new CargaFp(l.numCar(), l.data(), l.valor(), l.status(), l.naoEncontrada(), l.bloqueada(),
                    notasPorCarga.getOrDefault(l.id(), Set.of()), l.pesoKg(), l.motorista(), l.placa(), l.motoristaExterno(),
                    l.origemCarga(), l.origemNumero(), l.valorWinThor(), l.transferida()));
        }
        return resultado;
    }

    private static Integer numeroOuNulo(String texto) {
        try {
            return texto == null ? null : Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long numeroLongOuNulo(String texto) {
        try {
            return texto == null ? null : Long.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record Linha(UUID id, Integer numCar, LocalDate data, java.math.BigDecimal valor, String status,
                         boolean naoEncontrada, boolean bloqueada, java.math.BigDecimal pesoKg, String origemCarga,
                         String origemNumero, java.math.BigDecimal valorWinThor, boolean transferida, String motorista,
                         boolean motoristaExterno, String placa) {
    }
}
