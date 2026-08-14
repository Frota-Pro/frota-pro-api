package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.NotaFiscalResumoResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Fala com o endpoint interno de nota fiscal da frota-pro-integradora.
 * Nunca guarda nada localmente — cada chamada busca na hora.
 */
@Slf4j
@Component
public class NotaFiscalWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public NotaFiscalWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${frotapro.integracao.nfe.timeout-ms:8000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public List<NotaFiscalResumoResponse> listar(Integer numeroCargaExterno, Integer codigoCliente) {
        String url = baseUrl() + "/winthor/cargas/" + numeroCargaExterno + "/notas?codigoCliente=" + codigoCliente;
        NotaResumoIntegradoraDto[] resposta = executar(() ->
                restTemplate.getForObject(url, NotaResumoIntegradoraDto[].class));

        if (resposta == null) return List.of();
        return List.of(resposta).stream()
                .map(dto -> NotaFiscalResumoResponse.builder()
                        .numeroNota(dto.getNumeroNota())
                        .serie(dto.getSerie())
                        .emailCliente(dto.getEmailCliente())
                        .build())
                .toList();
    }

    public String buscarXml(Integer numeroCargaExterno, Long numeroNota) {
        String url = baseUrl() + "/winthor/cargas/" + numeroCargaExterno + "/notas/" + numeroNota + "/xml";
        return executar(() -> restTemplate.getForObject(url, String.class));
    }

    public byte[] buscarPdf(Integer numeroCargaExterno, Long numeroNota, byte[] logoBytes) {
        String url = baseUrl() + "/winthor/cargas/" + numeroCargaExterno + "/notas/" + numeroNota + "/pdf";

        Map<String, String> body = (logoBytes != null && logoBytes.length > 0)
                ? Map.of("logoBase64", Base64.getEncoder().encodeToString(logoBytes))
                : Map.of();

        ResponseEntity<byte[]> resposta = executar(() -> restTemplate.postForEntity(url, body, byte[].class));
        return resposta != null ? resposta.getBody() : null;
    }

    private <T> T executar(java.util.function.Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFound("Nota fiscal não encontrada (pode estar cancelada ou sem XML autorizado).");
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class NotaResumoIntegradoraDto {
        private Long numeroNota;
        private String serie;
        private Integer codigoCliente;
        private String nomeCliente;
        private String emailCliente;
    }
}
