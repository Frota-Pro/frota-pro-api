package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParametroSistemaUpdateRequest {

    @NotNull(message = "Dias de antecedência de vencimento de documento é obrigatório")
    @Min(value = 0, message = "Dias de antecedência deve ser >= 0")
    private Integer diasAntecedenciaVencimentoDocumento;

    @NotNull(message = "Km de antecedência de manutenção preventiva é obrigatório")
    @Min(value = 0, message = "Km de antecedência deve ser >= 0")
    private Integer kmAntecedenciaManutencaoPreventiva;

    @NotNull(message = "Dias de manutenção estagnada é obrigatório")
    @Min(value = 0, message = "Dias de manutenção estagnada deve ser >= 0")
    private Integer diasManutencaoEstagnada;

    @NotNull(message = "Dias de antecedência de prazo de multa é obrigatório")
    @Min(value = 0, message = "Dias de antecedência deve ser >= 0")
    private Integer diasAntecedenciaPrazoMulta;

    @NotNull(message = "Informe se a validação de motivo de alteração de peso/valor está ativa")
    private Boolean validarMotivoAlteracaoPesoValorCarga;

    @Size(max = 500, message = "Códigos de devolução permitidos deve ter no máximo 500 caracteres")
    private String codigosDevolucaoPermitidos;

    @NotNull(message = "Informe se transferência de pedido autoriza a atualização")
    private Boolean permitirAtualizacaoPorTransferencia;

    @NotNull(message = "Informe se a validação de tempo mínimo de entrega está ativa")
    private Boolean validarTempoMinimoCarga;

    @NotNull(message = "Tempo mínimo padrão de entrega é obrigatório")
    @Min(value = 0, message = "Tempo mínimo padrão deve ser >= 0")
    private Integer tempoMinimoEntregaPadraoMinutos;

    @NotNull(message = "Dias de retenção da auditoria é obrigatório")
    @Min(value = 30, message = "Dias de retenção da auditoria deve ser >= 30")
    private Integer diasRetencaoAuditoria;
}
