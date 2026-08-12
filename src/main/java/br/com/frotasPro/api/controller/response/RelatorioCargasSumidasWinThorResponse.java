package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCargasSumidasWinThorResponse {

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
        private String codigoMotorista;
        private String nomeMotorista;
        private String codigoCaminhao;
        private String placaCaminhao;
        private String codigoRota;
        private LocalDateTime dataVerificacaoWinThor;
    }
}
