package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSistemaResponse {

    private int diasAntecedenciaVencimentoDocumento;
    private int kmAntecedenciaTrocaPneu;
    private int diasManutencaoEstagnada;
    private int diasAntecedenciaPrazoMulta;

    private boolean validarMotivoAlteracaoPesoValorCarga;
    private String codigosDevolucaoPermitidos;
    private boolean permitirAtualizacaoPorTransferencia;

    private boolean validarTempoMinimoCarga;
    private int tempoMinimoEntregaPadraoMinutos;

    private int diasRetencaoAuditoria;
}
