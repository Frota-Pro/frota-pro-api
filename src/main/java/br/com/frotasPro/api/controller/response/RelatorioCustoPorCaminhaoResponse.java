package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioCustoPorCaminhaoResponse {
    private String codigoCaminhao;
    private String placaCaminhao;

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private BigDecimal totalCombustivel;
    private BigDecimal totalManutencao;
    private BigDecimal totalGeral;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private LocalDate data;
        private String tipo; // ABASTECIMENTO / MANUTENCAO
        private String descricao;
        private BigDecimal valor;
    }
}
