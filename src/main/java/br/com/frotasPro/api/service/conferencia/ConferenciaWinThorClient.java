package br.com.frotasPro.api.service.conferencia;

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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fala com os endpoints de conferência da frota-pro-integradora. Só leitura: a integradora procura no WinThor o que
 * parece errado; quem decide e altera o FrotaPRO é a API, depois que o usuário confirma.
 */
@Slf4j
@Component
public class ConferenciaWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public ConferenciaWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            // Um período inteiro de transferências pode levar mais que as consultas pontuais do sync.
            @Value("${frotapro.integracao.conferencia.timeout-ms:60000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(Math.min(timeoutMs, 10_000)))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    /**
     * Carregamentos cujas notas entraram depois da geração do MDF-e e cujo motorista/veículo diferem dos do manifesto.
     * Informe o período (data em que a nota entrou no carregamento) OU os números dos carregamentos.
     */
    public List<ReentregaMotoristaCaminhaoItem> reentregaMotoristaCaminhao(LocalDate dataInicial,
                                                                           LocalDate dataFinal,
                                                                           List<Integer> numCars) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(baseUrl() + "/winthor/conferencias/reentrega-motorista-caminhao");
        if (numCars != null && !numCars.isEmpty()) {
            uri.queryParam("numCars", numCars.stream().map(String::valueOf).collect(Collectors.joining(",")));
        } else {
            uri.queryParam("dataInicial", dataInicial).queryParam("dataFinal", dataFinal);
        }
        String url = uri.build().toUriString();

        Resposta resposta = executar(() -> restTemplate.getForObject(url, Resposta.class));
        return resposta != null && resposta.getItens() != null ? resposta.getItens() : List.of();
    }

    private <T> T executar(java.util.function.Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            throw new IntegracaoIndisponivelException(
                    "Sistema do WinThor indisponível no momento. Tente novamente em alguns minutos.", e);
        } catch (ResourceAccessException e) {
            log.error("Falha de rede ao chamar a integradora WinThor (conferência)", e);
            throw new IntegracaoIndisponivelException(
                    "Não foi possível falar com a integradora WinThor no momento. Tente novamente em alguns minutos.", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro inesperado da integradora WinThor (conferência): {}", e.getStatusCode(), e);
            throw new IntegracaoIndisponivelException(
                    "Erro ao consultar a conferência no WinThor. Tente novamente em alguns minutos.", e);
        }
    }

    private String baseUrl() {
        return integradoraBaseUrl.endsWith("/")
                ? integradoraBaseUrl.substring(0, integradoraBaseUrl.length() - 1)
                : integradoraBaseUrl;
    }

    @Data
    @NoArgsConstructor
    static class Resposta {
        private List<ReentregaMotoristaCaminhaoItem> itens;
    }

    /** Espelho do ConferenciaMotoristaCaminhaoItemDto da integradora. */
    @Data
    @NoArgsConstructor
    public static class ReentregaMotoristaCaminhaoItem {
        private Integer numCar;

        private Long numMdfe;
        private String situacaoMdfe;
        private LocalDateTime dataGeracaoMdfe;

        private Integer codMotoristaMdfe;
        private String nomeMotoristaMdfe;
        private String codVeiculoMdfe;
        private String placaMdfe;

        private Integer codMotoristaCarregamento;
        private String nomeMotoristaCarregamento;
        private String codVeiculoCarregamento;
        private String placaCarregamento;

        private boolean motoristaDiferente;
        private boolean veiculoDiferente;

        private List<Transferencia> transferencias;
    }

    @Data
    @NoArgsConstructor
    public static class Transferencia {
        private Long numNota;
        private Integer numCarAnterior;
        private Integer numCarAtual;
        private LocalDateTime dataTransferencia;
        private String motivo;
    }
}
