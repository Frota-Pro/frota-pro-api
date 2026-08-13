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
public class AnalyticsFrotaResponse {

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    // Totais do período — o "bater o olho" da tela.
    private Long totalCargasFinalizadas;
    private Long totalKmRodado;
    private BigDecimal totalLitros;
    private BigDecimal totalCustoCombustivel;
    private BigDecimal mediaKmPorLitro;

    /** Evolução semana a semana — alimenta os gráficos. */
    private List<PontoSemanal> serieSemanal;

    /** Top 5 / piores 5 motoristas do período, por km rodado. */
    private List<RankingMotoristaItem> topMotoristas;
    private List<RankingMotoristaItem> piorMotoristas;

    /** Top 5 / piores 5 caminhões do período, por consumo médio (km/L). */
    private List<RankingCaminhaoItem> topCaminhoesConsumo;
    private List<RankingCaminhaoItem> piorCaminhoesConsumo;

    @Getter
    @Setter
    @Builder
    public static class PontoSemanal {
        private LocalDate inicioSemana;
        private Long cargasFinalizadas;
        private Long kmRodado;
        private BigDecimal litros;
        private BigDecimal custoCombustivel;
    }

    @Getter
    @Setter
    @Builder
    public static class RankingMotoristaItem {
        private String codigoMotorista;
        private String nomeMotorista;
        private Long totalCargas;
        private Long totalKmRodado;
        private BigDecimal totalTonelada;
    }

    @Getter
    @Setter
    @Builder
    public static class RankingCaminhaoItem {
        private String caminhao;
        private BigDecimal mediaKmPorLitro;
        private BigDecimal totalLitros;
    }
}
