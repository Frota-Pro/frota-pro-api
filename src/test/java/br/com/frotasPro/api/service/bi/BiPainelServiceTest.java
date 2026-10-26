package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.config.BiProperties;
import br.com.frotasPro.api.controller.response.BiPainelResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BiPainelServiceTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 23);

    private MetabaseCliente cliente;
    private BiPainelService service;

    private static JsonNode json(String texto) {
        try {
            return JSON.readTree(texto);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @BeforeEach
    void setUp() {
        BiProperties props = new BiProperties();
        props.setMetabaseSiteUrl("http://metabase.local:3000");
        props.setEmbeddingSecret("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        BiProperties.Dashboard d = new BiProperties.Dashboard();
        d.setId(12);
        d.setTitulo("Previsto x realizado");
        BiProperties.Dashboard restrito = new BiProperties.Dashboard();
        restrito.setId(7);
        restrito.setTitulo("Integracao");
        restrito.setSomenteAdmin(true);
        props.setDashboards(List.of(d, restrito));

        cliente = mock(MetabaseCliente.class);
        service = new BiPainelService(new MetabaseEmbedService(props), cliente, JSON);

        when(cliente.dashboard(anyString())).thenReturn(json("""
                {"name": "FrotaPRO — previsto × realizado",
                 "parameters": [{"slug": "ultimos_meses"}, {"slug": "de"}, {"slug": "ate"}, {"slug": "caminhao"}],
                 "dashcards": [
                   {"id": 30, "card_id": 3, "row": 9, "col": 0, "card": {"name": "Viagens atrasadas", "display": "table"}},
                   {"id": 10, "card_id": null, "row": 0, "col": 0, "visualization_settings": {"text": "**Como ler:** previsto = mediana"}},
                   {"id": 20, "card_id": 2, "row": 3, "col": 6, "card": {"name": "Atraso médio", "display": "scalar"}},
                   {"id": 21, "card_id": 1, "row": 3, "col": 0, "card": {"name": "Viagens no prazo", "display": "scalar"}}
                 ]}
                """));
        when(cliente.cartao(anyString(), eq(21), eq(1), anyString())).thenReturn(json("""
                {"data": {"cols": [{"name": "pct", "display_name": "% no prazo", "base_type": "type/Decimal"}], "rows": [[71.4]]}}
                """));
        when(cliente.cartao(anyString(), eq(20), eq(2), anyString())).thenReturn(json("""
                {"data": {"cols": [{"name": "dias", "display_name": "Dias", "base_type": "type/Integer"}], "rows": [[3]]}}
                """));
        when(cliente.cartao(anyString(), eq(30), eq(3), anyString())).thenReturn(json("""
                {"data": {"cols": [{"name": "carga", "display_name": "Carga", "base_type": "type/Text"},
                                   {"name": "dia", "display_name": "Dia", "base_type": "type/Date"},
                                   {"name": "atraso", "display_name": "Atraso", "base_type": "type/BigInteger"}],
                          "rows": [["CAR-1", "2026-10-01", 4], ["CAR-2", null, 9]]}}
                """));
    }

    @Test
    void devolveOsBlocosNaOrdemDoDashboardETiposCertos() {
        BiPainelResponse r = service.buscar(12, false, BiPeriodo.resolver(6, null, null, HOJE));

        assertEquals("FrotaPRO — previsto × realizado", r.titulo());
        assertEquals(List.of("texto", "scalar", "scalar", "table"), r.blocos().stream().map(BiPainelResponse.Bloco::tipo).toList());
        assertEquals(List.of("Viagens no prazo", "Atraso médio"), r.blocos().subList(1, 3).stream().map(BiPainelResponse.Bloco::titulo).toList());

        assertEquals("**Como ler:** previsto = mediana", r.blocos().get(0).texto());
        assertEquals(71.4, r.blocos().get(1).linhas().get(0).get(0));
        assertEquals(3L, r.blocos().get(2).linhas().get(0).get(0));

        var tabela = r.blocos().get(3);
        assertEquals(List.of("texto", "data", "numero"), tabela.colunas().stream().map(BiPainelResponse.Coluna::tipo).toList());
        assertEquals("% no prazo", r.blocos().get(1).colunas().get(0).rotulo());
        assertEquals(2, tabela.totalLinhas());
        assertNull(tabela.linhas().get(1).get(1));
        assertEquals(9L, tabela.linhas().get(1).get(2));
    }

    @Test
    void ultimosMesesVaiComoListaEOsOutrosFiltrosNulos() {
        service.buscar(12, false, BiPeriodo.resolver(3, null, null, HOJE));

        verify(cliente).cartao(anyString(), eq(21), eq(1), eq("{\"ultimos_meses\":[3],\"de\":null,\"ate\":null,\"caminhao\":null}"));
    }

    @Test
    void periodoPersonalizadoVaiComoDatasESemUltimosMeses() {
        service.buscar(12, false, BiPeriodo.resolver(null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), HOJE));

        verify(cliente).cartao(anyString(), eq(21), eq(1), eq("{\"ultimos_meses\":null,\"de\":\"2026-10-01\",\"ate\":\"2026-10-15\",\"caminhao\":null}"));
    }

    @Test
    void cartaoQueFalhaViraBlocoComErroEOrestoSegue() {
        when(cliente.cartao(anyString(), eq(20), eq(2), anyString())).thenThrow(new IntegracaoIndisponivelException("O Metabase respondeu 500."));

        BiPainelResponse r = service.buscar(12, false, BiPeriodo.resolver(6, null, null, HOJE));

        assertEquals(4, r.blocos().size());
        var falho = r.blocos().get(2);
        assertEquals("Atraso médio", falho.titulo());
        assertEquals("O Metabase respondeu 500.", falho.erro());
        assertTrue(falho.linhas().isEmpty());
        assertNull(r.blocos().get(1).erro());
    }

    @Test
    void limitaAsLinhasEnviadasMasInformaOTotal() {
        StringBuilder linhas = new StringBuilder("[");
        for (int i = 0; i < 150; i++) linhas.append(i == 0 ? "" : ",").append("[\"CAR-").append(i).append("\", 1, 1]");
        linhas.append("]");
        when(cliente.cartao(anyString(), eq(30), eq(3), anyString())).thenReturn(json(
                "{\"data\": {\"cols\": [{\"name\":\"a\",\"base_type\":\"type/Text\"},{\"name\":\"b\",\"base_type\":\"type/Integer\"},{\"name\":\"c\",\"base_type\":\"type/Integer\"}], \"rows\": " + linhas + "}}"));

        var tabela = service.buscar(12, false, BiPeriodo.resolver(6, null, null, HOJE)).blocos().get(3);

        assertEquals(BiPainelService.MAX_LINHAS, tabela.linhas().size());
        assertEquals(150, tabela.totalLinhas());
    }

    @Test
    void mesmaConsultaEmSeguidaUsaOCacheDeUmMinuto() {
        var periodo = BiPeriodo.resolver(6, null, null, HOJE);

        service.buscar(12, false, periodo);
        service.buscar(12, false, periodo);

        verify(cliente, times(1)).dashboard(anyString());

        service.buscar(12, false, BiPeriodo.resolver(3, null, null, HOJE));
        verify(cliente, times(2)).dashboard(anyString());
    }

    @Test
    void dashboardRestritoSoParaAdminEMesmoComCacheAindaChecaPerfil() {
        var periodo = BiPeriodo.resolver(6, null, null, HOJE);
        service.buscar(7, true, periodo);

        assertThrows(AccessDeniedException.class, () -> service.buscar(7, false, periodo));
    }

    @Test
    void dashboardForaDoCatalogoNaoExisteESemConfigurarOBiFicaDesligado() {
        assertThrows(ObjectNotFound.class, () -> service.buscar(999, true, BiPeriodo.resolver(6, null, null, HOJE)));

        BiProperties vazio = new BiProperties();
        var desligado = new BiPainelService(new MetabaseEmbedService(vazio), cliente, JSON);
        assertThrows(IntegracaoIndisponivelException.class, () -> desligado.buscar(12, true, BiPeriodo.resolver(6, null, null, HOJE)));
    }

    @Test
    void oTokenNuncaChegaNaRespostaEPedeOsDadosComElePrimeiro() {
        BiPainelResponse r = service.buscar(12, false, BiPeriodo.resolver(6, null, null, HOJE));

        verify(cliente).dashboard(contains("."));
        assertFalse(r.toString().contains("eyJ"));
        assertNotNull(r.geradoEm());
    }

    @Test
    void tipoDaColunaSegueOBaseTypeDoMetabase() {
        assertEquals("numero", BiPainelService.tipoDaColuna("type/Integer"));
        assertEquals("numero", BiPainelService.tipoDaColuna("type/BigInteger"));
        assertEquals("numero", BiPainelService.tipoDaColuna("type/Decimal"));
        assertEquals("numero", BiPainelService.tipoDaColuna("type/Float"));
        assertEquals("data", BiPainelService.tipoDaColuna("type/Date"));
        assertEquals("data", BiPainelService.tipoDaColuna("type/DateTime"));
        assertEquals("texto", BiPainelService.tipoDaColuna("type/Text"));
        assertEquals("texto", BiPainelService.tipoDaColuna(""));
    }

    @Test
    void parametrosSemSlugOuVaziosNaoQuebram() {
        String s = service.parametrosJson(json("[{\"slug\":\"\"},{}]"), BiPeriodo.resolver(6, null, null, HOJE));

        assertEquals("{}", s);
        assertEquals("{}", service.parametrosJson(json("null"), BiPeriodo.resolver(6, null, null, HOJE)));
        verify(cliente, times(0)).cartao(anyString(), anyInt(), anyInt(), anyString());
    }
}
