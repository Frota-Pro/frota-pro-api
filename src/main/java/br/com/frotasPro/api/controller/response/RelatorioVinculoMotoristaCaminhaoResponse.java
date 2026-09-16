package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioVinculoMotoristaCaminhaoResponse {

    private LocalDate geradoEm;

    private long totalMotoristasAtivos;
    private long totalCaminhoesAtivos;
    private long totalComVinculo;
    private long totalMotoristasSemCaminhao;
    private long totalCaminhoesSemMotorista;

    private List<Linha> linhas;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Linha {
        /** MOTORISTA_COM_CAMINHAO | MOTORISTA_SEM_CAMINHAO | CAMINHAO_SEM_MOTORISTA */
        private String tipo;
        private String codigoMotorista;
        private String nomeMotorista;
        private String codigoCaminhao;
        private String placaCaminhao;
        private String observacao;
    }
}
