package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioVidaUtilPneuResponse {

    private String filtroCaminhao;
    private String filtroPneu;
    private Long totalPneus;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private String codigoPneu;
        private String numeroSerie;
        private String marca;
        private String modelo;
        private String medida;
        private String status;
        private String caminhao;
        private BigDecimal kmTotal;
        private BigDecimal kmMeta;
        private BigDecimal percentualVida;
    }
}
