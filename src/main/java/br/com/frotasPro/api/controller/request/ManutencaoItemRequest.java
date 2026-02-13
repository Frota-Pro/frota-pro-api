package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.TipoItemManutencao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManutencaoItemRequest {

    @NotNull(message = "Tipo do item é obrigatório")
    private TipoItemManutencao tipo;

    @NotBlank(message = "Descrição do item é obrigatória")
    @Size(min = 2, max = 200, message = "Descrição do item deve ter entre 2 e 200 caracteres")
    private String descricao;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor unitário deve ser >= 0")
    private BigDecimal valorUnitario;
}
