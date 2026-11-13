package br.com.frotasPro.api.service.planejamento;

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
import java.util.List;
import java.util.function.Supplier;

/**
 * Lê da frota-pro-integradora os carregamentos do WinThor para o planejamento. Só leitura: nada daqui altera o WinThor.
 */
@Slf4j
@Component
public class PlanejamentoWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public PlanejamentoWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${frotapro.integracao.planejamento.timeout-ms:60000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(Math.min(timeoutMs, 10_000)))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    /** Carregamentos montados entre as duas datas (inclusive). */
    public List<CarregamentoWinThor> carregamentosDoPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        String url = UriComponentsBuilder.fromUriString(baseUrl() + "/winthor/planejamento/carregamentos")
                .queryParam("dataInicial", dataInicial)
                .queryParam("dataFinal", dataFinal)
                .build().toUriString();
        return buscar(url);
    }

    private List<CarregamentoWinThor> buscar(String url) {
        Resposta resposta = executar(() -> restTemplate.getForObject(url, Resposta.class));
        return resposta != null && resposta.getItens() != null ? resposta.getItens() : List.of();
    }

    private <T> T executar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            throw new IntegracaoIndisponivelException(
                    "Sistema do WinThor indisponível no momento. Tente novamente em alguns minutos.", e);
        } catch (ResourceAccessException e) {
            log.error("Falha de rede ao chamar a integradora WinThor (planejamento)", e);
            throw new IntegracaoIndisponivelException(
                    "Não foi possível falar com a integradora WinThor no momento. Tente novamente em alguns minutos.", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro inesperado da integradora WinThor (planejamento): {}", e.getStatusCode(), e);
            throw new IntegracaoIndisponivelException(
                    "Erro ao consultar os carregamentos no WinThor. Tente novamente em alguns minutos.", e);
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
        private List<CarregamentoWinThor> itens;
    }

    /** Espelho do PlanejamentoCarregamentoDto da integradora. */
    @Data
    @NoArgsConstructor
    public static class CarregamentoWinThor {
        private Integer numCar;
        private LocalDate dataMontagem;
        private String destino;
        /** Motorista/veículo gravados no carregamento; vazio ou zero nos dois = venda de balcão. */
        private Integer codMotorista;
        private Integer codVeiculo;
        /** Nome do motorista e placa do veículo gravados no carregamento (null se o WinThor não achar). */
        private String nomeMotorista;
        private String placaVeiculo;
        /** QUILOS. */
        private Double pesoKg;
        private Integer totalPedidos;
        private Integer totalClientes;
        /** Soma do valor dos pedidos, em reais. */
        private java.math.BigDecimal valorTotal;
        private List<Integer> codigosClientes;
        private boolean faturada;
    }
}
