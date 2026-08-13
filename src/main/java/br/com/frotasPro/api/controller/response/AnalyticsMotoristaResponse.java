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
public class AnalyticsMotoristaResponse {

    private String codigoMotorista;
    private String nomeMotorista;

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private Long totalCargas;
    private Long totalKmRodado;
    private BigDecimal totalTonelada;
    private BigDecimal totalValorCargas;

    private Long cargasNoPrazo;
    private BigDecimal mediaDiasAtrasoChegada;
    private BigDecimal percentualCargasNoPrazo;

    private BigDecimal totalLitros;
    private BigDecimal mediaKmPorLitro;

    /** Comparação — média de km rodado por carga na frota toda, no mesmo período. */
    private BigDecimal mediaKmPorCargaFrota;
    private BigDecimal mediaKmPorCargaMotorista;

    private List<PontoSemanal> serieSemanal;

    @Getter
    @Setter
    @Builder
    public static class PontoSemanal {
        private LocalDate inicioSemana;
        private Long cargasFinalizadas;
        private Long kmRodado;
    }
}
