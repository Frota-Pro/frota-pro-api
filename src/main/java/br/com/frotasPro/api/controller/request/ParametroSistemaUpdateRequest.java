package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
}
