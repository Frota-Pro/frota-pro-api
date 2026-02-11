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
public class RelatorioRankingMotoristasResponse {
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private Long totalMotoristas;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private Long posicao;
        private String codigoMotorista;
        private String nomeMotorista;

        private Long totalCargas;
        private BigDecimal totalTonelada;
        private Long totalKmRodado;
        private BigDecimal totalValorCargas;
    }
}
