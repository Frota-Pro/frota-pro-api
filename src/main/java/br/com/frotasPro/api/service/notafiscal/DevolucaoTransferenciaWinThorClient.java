package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.DevolucaoResponse;
import br.com.frotasPro.api.controller.response.TransferenciaResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.excption.ObjectNotFound;
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

/**
 * Fala com o endpoint interno de devolução/transferência da
 * frota-pro-integradora. Nunca guarda nada localmente — cada chamada busca
 * na hora.
 */
@Slf4j
@Component
public class DevolucaoTransferenciaWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public DevolucaoTransferenciaWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${frotapro.integracao.nfe.timeout-ms:8000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public List<DevolucaoResponse> buscarDevolucoes(Integer numeroCargaExterno) {
        String url = baseUrl() + "/winthor/cargas/" + numeroCargaExterno + "/devolucoes";
        DevolucaoResponse[] resposta = executar(() -> restTemplate.getForObject(url, DevolucaoResponse[].class));
        return resposta != null ? List.of(resposta) : List.of();
    }

    public List<TransferenciaResponse> buscarTransferencias(Integer numeroCargaExterno) {
        String url = baseUrl() + "/winthor/cargas/" + numeroCargaExterno + "/transferencias";
        TransferenciaResponse[] resposta = executar(() -> restTemplate.getForObject(url, TransferenciaResponse[].class));
        return resposta != null ? List.of(resposta) : List.of();
    }

    private <T> T executar(java.util.function.Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFound("Carga não encontrada no WinThor.");
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
                    "Erro ao buscar dados fiscais no WinThor. Tente novamente em alguns minutos.", e);
        }
    }

    private String baseUrl() {
        return integradoraBaseUrl.endsWith("/")
                ? integradoraBaseUrl.substring(0, integradoraBaseUrl.length() - 1)
                : integradoraBaseUrl;
    }
}
