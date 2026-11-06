package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Cargas que o motorista já iniciou (ou finalizou) e que, no WinThor, estão num carregamento genérico de reentrega. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCargasReentregaConferirResponse {

    private Long total;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String numeroCarga;
        private String numeroCargaExterno;
        private String statusCarga;
        private LocalDate dtSaida;
        private BigDecimal pesoCarga;
        private BigDecimal valorTotal;
        private Integer kmInicial;
        private Integer kmFinal;
        private String codigoMotorista;
        private String nomeMotorista;
        private String codigoCaminhao;
        private String placaCaminhao;
    }
}
