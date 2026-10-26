package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.controller.response.BiOperacaoResponse;
import br.com.frotasPro.api.excption.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BiOperacaoServiceTest {

    private static final OffsetDateTime AGORA = OffsetDateTime.parse("2026-10-23T10:00:00Z");
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 23);

    private static Map<String, Object> linha(Object... pares) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put((String) pares[i], pares[i + 1]);
        }
        return m;
    }

    @Test
    void montaOsKpisComOsTiposDoBanco() {
        var r = BiOperacaoService.montar(BiPeriodo.resolver(6, null, null, HOJE), AGORA,
                linha("cargas", 1017L, "entregas", new BigDecimal("21361"), "notas", 35992L, "km", new BigDecimal("263214"), "custo_por_km", new BigDecimal("1.47")),
                linha("litros", new BigDecimal("58309"), "gasto", new BigDecimal("399194.49"), "preco_medio", new BigDecimal("6.85")),
                List.of(), List.of(), List.of(), List.of(), List.of());

        assertEquals(Integer.valueOf(6), r.meses());
        assertEquals(LocalDate.of(2026, 4, 23), r.inicio());
        assertEquals(HOJE, r.fim());
        assertEquals(AGORA, r.geradoEm());
        assertEquals(1017, r.kpis().cargas());
        assertEquals(21361, r.kpis().entregas());
        assertEquals(35992, r.kpis().notasFiscais());
        assertEquals(263214, r.kpis().kmRodados());
        assertEquals(58309.0, r.kpis().litros());
        assertEquals(399194.49, r.kpis().gastoCombustivel());
        assertEquals(6.85, r.kpis().precoMedioLitro());
        assertEquals(1.47, r.kpis().custoPorKm());
    }

    @Test
    void semDadosOsKpisFicamZeradosOuNulos() {
        var r = BiOperacaoService.montar(BiPeriodo.resolver(3, null, null, HOJE), AGORA, Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        assertEquals(0, r.kpis().cargas());
        assertEquals(0, r.kpis().kmRodados());
        assertNull(r.kpis().litros());
        assertNull(r.kpis().gastoCombustivel());
        assertNull(r.kpis().custoPorKm());
        assertTrue(r.cargasPorMes().isEmpty());
        assertTrue(r.metasPorMes().isEmpty());
    }

    @Test
    void montaAsSeriesMantendoAOrdemDoBanco() {
        var r = BiOperacaoService.montar(BiPeriodo.resolver(6, null, null, HOJE), AGORA, Map.of(), Map.of(),
                List.of(linha("mes", "2026-08", "cargas", 7278L, "entregas", 160, "notas", 300), linha("mes", "2026-09", "cargas", 7912L, "entregas", 290L, "notas", 310L)),
                List.of(linha("mes", "2026-08", "litros", new BigDecimal("1000"), "gasto", new BigDecimal("6000.5"), "preco_medio", new BigDecimal("6.00"))),
                List.of(linha("rota", "ROT-000004 · SOUSA", "cargas", 73L)),
                List.of(linha("categoria", "TOCO", "km_por_litro", new BigDecimal("4.10"), "meta_km_por_litro", new BigDecimal("4.15"), "percentual_meta", new BigDecimal("98.8"), "viagens", 24L),
                        linha("categoria", "Sem categoria", "km_por_litro", new BigDecimal("3.50"), "meta_km_por_litro", null, "percentual_meta", null, "viagens", 3L)),
                List.of(linha("mes", "2026-09", "consumo_pct", new BigDecimal("70.8"), "toneladas_pct", new BigDecimal("12.5"), "sem_dados", 8L)));

        assertEquals(List.of("2026-08", "2026-09"), r.cargasPorMes().stream().map(BiOperacaoResponse.MesCargas::mes).toList());
        assertEquals(160, r.cargasPorMes().get(0).entregas());
        assertEquals(7912, r.cargasPorMes().get(1).cargas());

        assertEquals(6000.5, r.combustivelPorMes().get(0).gasto());
        assertEquals("ROT-000004 · SOUSA", r.cargasPorRota().get(0).rota());
        assertEquals(73, r.cargasPorRota().get(0).cargas());

        assertEquals("TOCO", r.consumoPorCategoria().get(0).categoria());
        assertEquals(98.8, r.consumoPorCategoria().get(0).percentualMeta());
        assertNull(r.consumoPorCategoria().get(1).metaKmPorLitro());
        assertNull(r.consumoPorCategoria().get(1).percentualMeta());

        assertEquals(70.8, r.metasPorMes().get(0).consumoPct());
        assertEquals(8, r.metasPorMes().get(0).semDados());
    }

    @Test
    void textoNuloViraVazio() {
        assertEquals("", BiOperacaoService.texto(null));
        assertEquals("2026-10", BiOperacaoService.texto("2026-10"));
    }

    @Test
    void periodoForaDosLimitesNaoConsultaOBanco() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        var service = new BiOperacaoService(jdbc);

        assertThrows(BusinessException.class, () -> service.buscar(0, null, null));
        assertThrows(BusinessException.class, () -> service.buscar(-3, null, null));
        assertThrows(BusinessException.class, () -> service.buscar(BiPeriodo.MESES_MAXIMO + 1, null, null));

        verify(jdbc, never()).queryForList(anyString(), any(MapSqlParameterSource.class));
    }

    @Test
    void buscaExecutaAsSeteConsultasComOPeriodoPedido() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class))).thenReturn(List.of());
        var service = new BiOperacaoService(jdbc);

        var r = service.buscar(12, null, null);

        assertEquals(Integer.valueOf(12), r.meses());
        assertEquals(0, r.kpis().cargas());
        verify(jdbc, org.mockito.Mockito.times(7)).queryForList(anyString(), org.mockito.ArgumentMatchers.argThat((MapSqlParameterSource p) -> p.getValue("inicio") instanceof LocalDate i && i.isBefore(LocalDate.now().minusMonths(11)) && p.hasValue("fim")));
    }

    @Test
    void todasAsConsultasExistemUsamOPeriodoEsoLeemDados() {
        List<String> consultas = List.of(
                BiOperacaoService.sqlComBase("_cargas_base", "kpis_cargas"),
                BiOperacaoService.sqlComBase("_abastecimentos_base", "kpis_combustivel"),
                BiOperacaoService.sqlComBase("_cargas_base", "cargas_por_mes"),
                BiOperacaoService.sqlComBase("_abastecimentos_base", "combustivel_por_mes"),
                BiOperacaoService.sqlComBase("_cargas_base", "cargas_por_rota"),
                BiOperacaoService.sql("consumo_por_categoria"),
                BiOperacaoService.sql("metas_por_mes"));

        for (String sql : consultas) {
            String minusculo = sql.toLowerCase();
            assertTrue(sql.contains(":inicio") && sql.contains(":fim"), "toda consulta filtra pelo periodo");
            assertTrue(minusculo.trim().startsWith("with"), "consulta de leitura");
            for (String proibido : List.of("insert ", "update ", "delete ", "drop ", "alter ", "truncate ", "create ")) {
                assertFalse(minusculo.contains(proibido), "consulta do BI nao pode escrever: " + proibido);
            }
        }
    }

    @Test
    void periodoPersonalizadoUsaAsDatasEscolhidasENaoOsMeses() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class))).thenReturn(List.of());
        var service = new BiOperacaoService(jdbc);

        var r = service.buscar(null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15));

        assertNull(r.meses());
        assertEquals(LocalDate.of(2026, 10, 1), r.inicio());
        assertEquals(LocalDate.of(2026, 10, 15), r.fim());
        verify(jdbc, org.mockito.Mockito.times(7)).queryForList(anyString(), org.mockito.ArgumentMatchers.argThat((MapSqlParameterSource p) ->
                LocalDate.of(2026, 10, 1).equals(p.getValue("inicio")) && LocalDate.of(2026, 10, 15).equals(p.getValue("fim"))));
    }

    @Test
    void regrasDeNegocioDoMetabaseContinuamNasConsultas() {
        String cargas = BiOperacaoService.sql("_cargas_base");
        assertTrue(cargas.contains("BETWEEN 1 AND 3000"), "km valido de 1 a 3000");
        assertTrue(cargas.contains("NOT COALESCE(a.preco_anomalo, false)"), "preco anomalo fora da soma");

        String consumo = BiOperacaoService.sql("consumo_por_categoria");
        assertTrue(consumo.contains("status_carga = 'FINALIZADA'"));
        assertTrue(consumo.contains("COALESCE(c.dt_competencia, c.data_saida)"), "carga conta pela competencia");

        String metas = BiOperacaoService.sql("metas_por_mes");
        assertTrue(metas.contains("realizado >= valor_meta"), "meta bate com maior ou igual");
    }
}
