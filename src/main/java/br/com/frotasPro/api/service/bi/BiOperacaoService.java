package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.controller.response.BiOperacaoResponse;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.ConsumoCategoria;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.Kpis;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.MesCargas;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.MesCombustivel;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.MesMetas;
import br.com.frotasPro.api.controller.response.BiOperacaoResponse.RotaCargas;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Números do painel "Operação" do gestor, direto do banco. As consultas são as mesmas do
 * dashboard do Metabase (src/main/resources/bi/operacao/*.sql), com o filtro de período
 * "últimos N meses"; o app só desenha.
 */
@Service
@RequiredArgsConstructor
public class BiOperacaoService {

    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public BiOperacaoResponse buscar(Integer meses, LocalDate de, LocalDate ate) {
        BiPeriodo periodo = BiPeriodo.resolver(meses, de, ate, FusoHorarioUtils.hojeBrasil());

        var parametros = new MapSqlParameterSource()
                .addValue("inicio", periodo.inicio())
                .addValue("fim", periodo.fimDaConsulta());

        Map<String, Object> kpisCargas = primeira(jdbc.queryForList(sqlComBase("_cargas_base", "kpis_cargas"), parametros));
        Map<String, Object> kpisCombustivel = primeira(jdbc.queryForList(sqlComBase("_abastecimentos_base", "kpis_combustivel"), parametros));
        List<Map<String, Object>> cargasPorMes = jdbc.queryForList(sqlComBase("_cargas_base", "cargas_por_mes"), parametros);
        List<Map<String, Object>> combustivelPorMes = jdbc.queryForList(sqlComBase("_abastecimentos_base", "combustivel_por_mes"), parametros);
        List<Map<String, Object>> cargasPorRota = jdbc.queryForList(sqlComBase("_cargas_base", "cargas_por_rota"), parametros);
        List<Map<String, Object>> consumo = jdbc.queryForList(sql("consumo_por_categoria"), parametros);
        List<Map<String, Object>> metas = jdbc.queryForList(sql("metas_por_mes"), parametros);

        return montar(periodo, OffsetDateTime.now(), kpisCargas, kpisCombustivel, cargasPorMes, combustivelPorMes, cargasPorRota, consumo, metas);
    }

    /** Converte as linhas do banco no JSON do app (separado da consulta para poder ser testado sem banco). */
    static BiOperacaoResponse montar(BiPeriodo periodo,
                                     OffsetDateTime geradoEm,
                                     Map<String, Object> kpisCargas,
                                     Map<String, Object> kpisCombustivel,
                                     List<Map<String, Object>> cargasPorMes,
                                     List<Map<String, Object>> combustivelPorMes,
                                     List<Map<String, Object>> cargasPorRota,
                                     List<Map<String, Object>> consumo,
                                     List<Map<String, Object>> metas) {

        Kpis kpis = new Kpis(
                inteiro(kpisCargas.get("cargas")),
                inteiro(kpisCargas.get("entregas")),
                inteiro(kpisCargas.get("notas")),
                inteiro(kpisCargas.get("km")),
                decimal(kpisCombustivel.get("litros")),
                decimal(kpisCombustivel.get("gasto")),
                decimal(kpisCombustivel.get("preco_medio")),
                decimal(kpisCargas.get("custo_por_km"))
        );

        return new BiOperacaoResponse(
                periodo.meses(),
                periodo.inicio(),
                periodo.fim(),
                geradoEm,
                kpis,
                cargasPorMes.stream()
                        .map(l -> new MesCargas(texto(l.get("mes")), inteiro(l.get("cargas")), inteiro(l.get("entregas")), inteiro(l.get("notas"))))
                        .toList(),
                combustivelPorMes.stream()
                        .map(l -> new MesCombustivel(texto(l.get("mes")), decimal(l.get("litros")), decimal(l.get("gasto")), decimal(l.get("preco_medio"))))
                        .toList(),
                cargasPorRota.stream()
                        .map(l -> new RotaCargas(texto(l.get("rota")), inteiro(l.get("cargas"))))
                        .toList(),
                consumo.stream()
                        .map(l -> new ConsumoCategoria(texto(l.get("categoria")), decimal(l.get("km_por_litro")), decimal(l.get("meta_km_por_litro")),
                                decimal(l.get("percentual_meta")), inteiro(l.get("viagens"))))
                        .toList(),
                metas.stream()
                        .map(l -> new MesMetas(texto(l.get("mes")), decimal(l.get("consumo_pct")), decimal(l.get("toneladas_pct")), inteiro(l.get("sem_dados"))))
                        .toList()
        );
    }

    private static Map<String, Object> primeira(List<Map<String, Object>> linhas) {
        return linhas.isEmpty() ? Map.of() : linhas.get(0);
    }

    static long inteiro(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0L;
    }

    static Double decimal(Object valor) {
        return valor instanceof Number n ? n.doubleValue() : null;
    }

    static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    static String sql(String nome) {
        try (var in = new ClassPathResource("bi/operacao/" + nome + ".sql").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Consulta do BI não encontrada: " + nome, e);
        }
    }

    static String sqlComBase(String base, String nome) {
        return sql(base) + sql(nome);
    }
}
