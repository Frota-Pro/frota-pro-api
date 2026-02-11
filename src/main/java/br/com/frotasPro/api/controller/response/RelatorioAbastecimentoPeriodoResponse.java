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
public class RelatorioAbastecimentoPeriodoResponse {
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private String filtroCaminhao;
    private String filtroMotorista;

    private BigDecimal totalLitros;
    private BigDecimal totalValor;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private LocalDate data;
        private String caminhao;   // placa/codigo
        private String motorista;  // nome/codigo
        private String posto;
        private String cidade;
        private BigDecimal litros;
        private BigDecimal valorTotal;
        private BigDecimal kmOdometro;
        private BigDecimal mediaKmLitro;
        private String tipoCombustivel;
    }
}
