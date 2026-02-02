package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.TipoItemManutencao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManutencaoItemRequest {

    @NotNull
    private TipoItemManutencao tipo;

    @NotBlank
    private String descricao;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal quantidade;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal valorUnitario;
}
