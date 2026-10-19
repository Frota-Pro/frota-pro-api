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
public class RelatorioCargasNaoSincronizadasResponse {

    private Long total;
    private Long totalSemMotorista;
    private Long totalSemCaminhao;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String numeroCargaExterno;
        private Long numMdfe;
        private String situacaoMdfe;
        private String motivo;
        private String motivoDescricao;
        private String codigoMotoristaExterno;
        private String nomeMotoristaExterno;
        private String codigoVeiculoExterno;
        private String placaVeiculoExterno;
        private LocalDate dtSaida;
        private BigDecimal pesoCarga;
        private BigDecimal valorTotal;
        private String destino;
        private Integer totalClientes;
        private Integer tentativas;
        private LocalDateTime primeiraOcorrenciaEm;
        private LocalDateTime ultimaOcorrenciaEm;
    }
}
