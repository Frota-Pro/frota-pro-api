package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarStatusMultaRequest {

    @NotNull(message = "Status é obrigatório")
    private StatusPagamentoMulta statusPagamento;
}
