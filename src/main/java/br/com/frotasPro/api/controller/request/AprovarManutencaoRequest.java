package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusAprovacaoManutencao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AprovarManutencaoRequest {

    @NotNull(message = "Informe se o orçamento foi aprovado ou rejeitado")
    private StatusAprovacaoManutencao statusAprovacao;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;
}
