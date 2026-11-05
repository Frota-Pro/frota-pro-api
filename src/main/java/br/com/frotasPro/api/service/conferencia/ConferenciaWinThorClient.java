package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.DevolucaoIn;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.ManifestoIn;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Nota;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Transferencia;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Busca na integradora as notas do WinThor de um período (só leitura), com as marcas do transporte. */
@Slf4j
@Component
public class ConferenciaWinThorClient {

    private final RestTemplate restTemplate;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    public ConferenciaWinThorClient(
            RestTemplateBuilder restTemplateBuilder,
            // Um mês inteiro são ~11 mil notas e o Oracle leva alguns segundos: bem acima do prazo das consultas pontuais.
            @Value("${frotapro.conferencia.timeout-ms:120000}") long timeoutMs
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    /** Notas do período e o log de transferência entre cargas delas (de qualquer data). */
    public record Dados(List<Nota> notas, List<Transferencia> transferencias, List<ManifestoIn> manifestos,
                        List<DevolucaoIn> devolucoes) {
    }

    public Dados buscar(LocalDate inicio, LocalDate fim) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl() + "/winthor/conferencia/notas")
                .queryParam("inicio", inicio)
                .queryParam("fim", fim)
                .toUriString();
        try {
            Resposta resposta = restTemplate.getForObject(url, Resposta.class);
            if (resposta == null || resposta.getNotas() == null) {
                return new Dados(List.of(), List.of(), List.of(), List.of());
            }
            List<Transferencia> transferencias = resposta.getTransferencias() == null ? List.of()
                    : resposta.getTransferencias().stream().map(TransferenciaIntegradora::paraTransferencia).toList();
            List<ManifestoIn> manifestos = resposta.getManifestosSemNota() == null ? List.of()
                    : resposta.getManifestosSemNota().stream().map(ManifestoIntegradora::paraManifesto).toList();
            List<DevolucaoIn> devolucoes = resposta.getDevolucoes() == null ? List.of()
                    : resposta.getDevolucoes().stream().map(DevolucaoIntegradora::paraDevolucao).toList();
            return new Dados(resposta.getNotas().stream().map(NotaIntegradora::paraNota).toList(), transferencias,
                    manifestos, devolucoes);
        } catch (ResourceAccessException | HttpServerErrorException.ServiceUnavailable e) {
            log.error("Falha ao falar com a integradora WinThor (conferência)", e);
            throw new IntegracaoIndisponivelException(
                    "Não foi possível falar com a integradora WinThor agora. Tente novamente em alguns minutos.", e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Erro da integradora WinThor na conferência: {}", e.getStatusCode(), e);
            throw new IntegracaoIndisponivelException(
                    "A integradora WinThor recusou a consulta de conferência (" + e.getStatusCode().value() + ").", e);
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
        private List<NotaIntegradora> notas;
        private List<TransferenciaIntegradora> transferencias;
        private List<ManifestoIntegradora> manifestosSemNota;
        private List<DevolucaoIntegradora> devolucoes;
    }

    @Data
    @NoArgsConstructor
    static class ManifestoIntegradora {
        private Long numMdfe;
        private Integer codMotorista;
        private String codVeiculo;
        private String situacao;
        private LocalDate data;
        private BigDecimal pesoKg;

        ManifestoIn paraManifesto() {
            return new ManifestoIn(numMdfe, codMotorista, codVeiculo, situacao, data, pesoKg);
        }
    }

    @Data
    @NoArgsConstructor
    static class DevolucaoIntegradora {
        private Long numNota;
        private BigDecimal valor;
        private BigDecimal pesoKg;

        DevolucaoIn paraDevolucao() {
            return new DevolucaoIn(numNota == null ? 0L : numNota, valor, pesoKg);
        }
    }

    @Data
    @NoArgsConstructor
    static class TransferenciaIntegradora {
        private Long numNota;
        private Integer numCarAnterior;
        private Integer numCarAtual;
        private LocalDateTime dtTransferencia;
        private String motivo;

        Transferencia paraTransferencia() {
            return new Transferencia(numNota == null ? 0L : numNota, numCarAnterior, numCarAtual, dtTransferencia, motivo);
        }
    }

    @Data
    @NoArgsConstructor
    static class NotaIntegradora {
        private Long numNota;
        private String especie;
        private LocalDate dtSaida;
        private Integer codCli;
        private Integer condVenda;
        private Integer numCar;
        private BigDecimal vlTotal;
        private boolean cancelada;
        private boolean temMotoristaVeiculo;
        private boolean temMdfe;
        private boolean temPedido;
        private boolean temCarregamento;
        private Integer codMotoristaMdfe;
        private String codVeiculoMdfe;

        Nota paraNota() {
            return new Nota(numNota == null ? 0L : numNota, especie, dtSaida, codCli, condVenda, numCar, vlTotal,
                    cancelada, temMotoristaVeiculo, temMdfe, temPedido, temCarregamento,
                    codMotoristaMdfe, codVeiculoMdfe, false, false);
        }
    }
}
