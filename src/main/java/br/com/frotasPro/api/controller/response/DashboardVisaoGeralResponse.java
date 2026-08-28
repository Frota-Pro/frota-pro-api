package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DashboardVisaoGeralResponse {

    private AlertasResumo alertas;
    private PneusResumo pneus;
    private FrotaResumo frota;
    private BigDecimal consumoMedioKmLMes;
    private ManutencoesResumo manutencoes;
    private MultasResumo multas;
    private List<CargaStatusResumo> cargasPorStatus;

    @Getter
    @Builder
    public static class AlertasResumo {
        private long cnhVencendo;
        private long documentosCaminhaoVencendo;
    }

    @Getter
    @Builder
    public static class PneusResumo {
        private long vencidos;
        private long proximoFim;
        private long ok;
    }

    @Getter
    @Builder
    public static class FrotaResumo {
        private long disponiveis;
        private long emRota;
        private long emManutencao;
        private long totalAtivos;
    }

    @Getter
    @Builder
    public static class ManutencoesResumo {
        private long abertas;
        private long atrasadas;
    }

    @Getter
    @Builder
    public static class MultasResumo {
        private long pendentes;
        private BigDecimal valorTotal;
        private LocalDate prazoMaisProximo;
    }

    @Getter
    @Builder
    public static class CargaStatusResumo {
        private String status;
        private String statusLabel;
        private long total;
    }
}
