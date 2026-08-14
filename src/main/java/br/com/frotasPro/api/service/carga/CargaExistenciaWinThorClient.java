package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fala com o endpoint interno de reconciliação de cargas da
 * frota-pro-integradora — verifica quais numcar já sincronizados antes
 * ainda existem no WinThor.
 */
@Slf4j
@Component
public class CargaExistenciaWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public CargaExistenciaWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${frotapro.integracao.nfe.timeout-ms:8000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    /** Dos códigos perguntados, retorna quais ainda existem no WinThor. */
    public Set<Integer> filtrarExistentes(List<Integer> codigosCargas) {
        String url = baseUrl() + "/winthor/cargas/verificar-existencia";

        Map<String, Object> body = Map.of("codigosCargas", codigosCargas);

        RespostaExistencia resposta = executar(() -> restTemplate.postForObject(url, body, RespostaExistencia.class));
        return resposta != null && resposta.getCodigosExistentes() != null
                ? Set.copyOf(resposta.getCodigosExistentes())
                : Set.of();
    }

    private <T> T executar(java.util.function.Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            throw new IntegracaoIndisponivelException(
                    "Sistema do WinThor indisponível no momento. Tente novamente em alguns minutos.", e);
        } catch (ResourceAccessException e) {
            log.error("Falha de rede ao chamar a integradora WinThor", e);
            throw new IntegracaoIndisponivelException(
                    "Não foi possível falar com a integradora WinThor no momento. Tente novamente em alguns minutos.", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro inesperado da integradora WinThor: {}", e.getStatusCode(), e);
            throw new IntegracaoIndisponivelException(
                    "Erro ao verificar existência de cargas no WinThor. Tente novamente em alguns minutos.", e);
        }
    }

    private String baseUrl() {
        return integradoraBaseUrl.endsWith("/")
                ? integradoraBaseUrl.substring(0, integradoraBaseUrl.length() - 1)
                : integradoraBaseUrl;
    }

    @Data
    @NoArgsConstructor
    private static class RespostaExistencia {
        private List<Integer> codigosExistentes;
    }
}
