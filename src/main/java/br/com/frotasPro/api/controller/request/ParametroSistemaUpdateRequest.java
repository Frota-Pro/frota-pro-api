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

    @NotNull(message = "Km de antecedência de troca de pneu é obrigatório")
    @Min(value = 0, message = "Km de antecedência deve ser >= 0")
    private Integer kmAntecedenciaTrocaPneu;

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
}
