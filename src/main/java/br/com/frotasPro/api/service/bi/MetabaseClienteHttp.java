package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.config.BiProperties;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetabaseClienteHttp implements MetabaseCliente {

    private static final Duration TIMEOUT = Duration.ofSeconds(25);

    private final BiProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    public JsonNode dashboard(String token) {
        return get("/api/embed/dashboard/" + token);
    }

    @Override
    public JsonNode cartao(String token, int dashcardId, int cardId, String parametrosJson) {
        String query = "?parameters=" + URLEncoder.encode(parametrosJson, StandardCharsets.UTF_8);
        return get("/api/embed/dashboard/" + token + "/dashcard/" + dashcardId + "/card/" + cardId + query);
    }

    private JsonNode get(String caminho) {
        // o token vai no caminho: nunca entra em mensagem de erro nem em log
        String descricao = caminho.replaceAll("/embed/dashboard/[^/?]+", "/embed/dashboard/<token>");
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(props.urlDaApiMetabase() + caminho))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // o Metabase responde 202 nas consultas de cartão; qualquer 2xx vale
            if (resp.statusCode() / 100 != 2) {
                log.warn("Metabase respondeu {} em {}", resp.statusCode(), descricao);
                throw new IntegracaoIndisponivelException("O Metabase respondeu " + resp.statusCode() + " (" + descricao + ").");
            }
            return objectMapper.readTree(resp.body());
        } catch (JsonProcessingException e) {
            throw new IntegracaoIndisponivelException("Resposta inválida do Metabase (" + descricao + ").");
        } catch (IOException e) {
            throw new IntegracaoIndisponivelException("Não foi possível falar com o Metabase (" + descricao + ").");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegracaoIndisponivelException("Consulta ao Metabase interrompida.");
        }
    }
}
