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
public class AnalyticsCaminhaoResponse {

    private String codigoCaminhao;
    private String placaCaminhao;
    private String descricaoCaminhao;

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private Long totalCargas;
    private Long totalKmRodado;

    private BigDecimal totalLitros;
    private BigDecimal totalCustoCombustivel;
    private BigDecimal mediaKmPorLitro;

    private BigDecimal totalCustoManutencao;
    private Long qtdManutencoes;

    /** Comparação — média de km/L da frota toda no mesmo período. */
    private BigDecimal mediaKmPorLitroFrota;

    private List<PontoSemanal> serieSemanal;

    @Getter
    @Setter
    @Builder
    public static class PontoSemanal {
        private LocalDate inicioSemana;
        private Long kmRodado;
        private BigDecimal litros;
        private BigDecimal custoCombustivel;
    }
}
