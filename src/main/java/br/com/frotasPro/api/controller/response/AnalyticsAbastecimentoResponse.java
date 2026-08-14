package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class AnalyticsAbastecimentoResponse {

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private BigDecimal totalLitros;
    private BigDecimal totalCusto;
    private BigDecimal mediaPrecoLitro;

    private List<PontoSemanal> serieSemanal;
    private List<ResumoPosto> porPosto;
    private List<ResumoCaminhao> porCaminhao;

    @Getter
    @Setter
    @Builder
    public static class PontoSemanal {
        private LocalDate inicioSemana;
        private BigDecimal litros;
        private BigDecimal custo;
    }

    @Getter
    @Setter
    @Builder
    public static class ResumoPosto {
        private String posto;
        private BigDecimal totalLitros;
        private BigDecimal totalValor;
    }

    @Getter
    @Setter
    @Builder
    public static class ResumoCaminhao {
        private String caminhao;
        private BigDecimal totalLitros;
        private BigDecimal totalValor;
        private BigDecimal mediaKmPorLitro;
    }
}
