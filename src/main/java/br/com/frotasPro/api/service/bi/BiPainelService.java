package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.controller.response.BiPainelResponse;
import br.com.frotasPro.api.controller.response.BiPainelResponse.Bloco;
import br.com.frotasPro.api.controller.response.BiPainelResponse.Coluna;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dados de um dashboard do Metabase para o app desenhar nativamente. A API pede ao Metabase os
 * cartões do dashboard (com o mesmo link assinado da incorporação, então valem as mesmas consultas
 * e regras do Metabase) e devolve só nome, tipo, colunas e linhas de cada um.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BiPainelService {

    static final int MAX_LINHAS = 60;
    private static final long CACHE_MS = 60_000;

    private final MetabaseEmbedService embedService;
    private final MetabaseCliente cliente;
    private final ObjectMapper objectMapper;

    private final ExecutorService executor = Executors.newFixedThreadPool(6);
    private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

    private record EntradaCache(long criadoEm, BiPainelResponse resposta) {
    }

    public BiPainelResponse buscar(int dashboardId, boolean admin, BiPeriodo periodo) {
        var acesso = embedService.acessoParaDados(dashboardId, admin);

        String chave = dashboardId + "|" + periodo;
        EntradaCache guardado = cache.get(chave);
        if (guardado != null && System.currentTimeMillis() - guardado.criadoEm() < CACHE_MS) {
            return guardado.resposta();
        }

        JsonNode dash = cliente.dashboard(acesso.token());
        String parametros = parametrosJson(dash.path("parameters"), periodo);

        List<JsonNode> cartoes = new ArrayList<>();
        dash.path("dashcards").forEach(cartoes::add);
        cartoes.sort(Comparator.<JsonNode>comparingInt(n -> n.path("row").asInt()).thenComparingInt(n -> n.path("col").asInt()));

        List<CompletableFuture<Bloco>> futuros = new ArrayList<>();
        for (JsonNode cartao : cartoes) {
            futuros.add(CompletableFuture.supplyAsync(() -> bloco(cartao, acesso.token(), parametros), executor));
        }

        List<Bloco> blocos = new ArrayList<>();
        for (var f : futuros) {
            Bloco b = f.join();
            if (b != null) blocos.add(b);
        }

        String titulo = dash.path("name").asText(acesso.titulo());
        var resposta = new BiPainelResponse(dashboardId, titulo, periodo.meses(), periodo.inicio(), periodo.fim(), OffsetDateTime.now(), blocos);

        if (cache.size() > 200) cache.clear();
        cache.put(chave, new EntradaCache(System.currentTimeMillis(), resposta));
        return resposta;
    }

    /** Monta o objeto {slug: valor} que o Metabase espera; o que o painel não usa fica nulo. */
    String parametrosJson(JsonNode parametros, BiPeriodo periodo) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (JsonNode p : parametros) {
            String slug = p.path("slug").asText("");
            if (slug.isEmpty()) continue;
            Object valor = switch (slug) {
                case "ultimos_meses" -> periodo.personalizado() ? null : List.of(periodo.meses());
                case "de" -> periodo.personalizado() ? periodo.inicio().toString() : null;
                case "ate" -> periodo.personalizado() ? periodo.fim().toString() : null;
                default -> null;
            };
            mapa.put(slug, valor);
        }
        try {
            return objectMapper.writeValueAsString(mapa);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    Bloco bloco(JsonNode dashcard, String token, String parametros) {
        JsonNode card = dashcard.path("card");
        String display = card.path("display").asText("");
        JsonNode cardId = dashcard.path("card_id");

        // cartão de texto do dashboard (explicações, títulos de seção)
        if (cardId.isNull() || cardId.isMissingNode()) {
            String texto = dashcard.path("visualization_settings").path("text").asText("");
            if (texto.isBlank()) return null;
            return new Bloco("texto", null, texto, null, List.of(), List.of(), 0);
        }

        String titulo = card.path("name").asText(dashcard.path("visualization_settings").path("card.title").asText(""));
        try {
            JsonNode resposta = cliente.cartao(token, dashcard.path("id").asInt(), cardId.asInt(), parametros);
            return montarBloco(display.isEmpty() ? "table" : display, titulo, resposta.path("data"));
        } catch (RuntimeException e) {
            log.warn("Cartão '{}' do dashboard falhou: {}", titulo, e.getMessage());
            return new Bloco(display.isEmpty() ? "table" : display, titulo, null, e.getMessage(), List.of(), List.of(), 0);
        }
    }

    static Bloco montarBloco(String tipo, String titulo, JsonNode data) {
        List<Coluna> colunas = new ArrayList<>();
        for (JsonNode c : data.path("cols")) {
            String nome = c.path("name").asText("");
            String rotulo = c.path("display_name").asText(nome);
            colunas.add(new Coluna(nome, rotulo, tipoDaColuna(c.path("base_type").asText(""))));
        }

        JsonNode rows = data.path("rows");
        int total = rows.size();
        List<List<Object>> linhas = new ArrayList<>();
        int limite = "scalar".equals(tipo) ? 1 : MAX_LINHAS;
        for (int i = 0; i < total && i < limite; i++) {
            List<Object> linha = new ArrayList<>();
            for (JsonNode v : rows.get(i)) {
                linha.add(valor(v));
            }
            linhas.add(linha);
        }
        return new Bloco(tipo, titulo, null, null, colunas, linhas, total);
    }

    static String tipoDaColuna(String baseType) {
        if (baseType.contains("Integer") || baseType.contains("Decimal") || baseType.contains("Float") || baseType.contains("Number")) {
            return "numero";
        }
        if (baseType.contains("Date") || baseType.contains("Time")) {
            return "data";
        }
        return "texto";
    }

    private static Object valor(JsonNode v) {
        if (v == null || v.isNull()) return null;
        if (v.isIntegralNumber()) return v.asLong();
        if (v.isNumber()) return v.asDouble();
        if (v.isBoolean()) return v.asBoolean();
        return v.asText();
    }
}
