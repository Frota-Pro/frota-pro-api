package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarObservacaoMotoristaRequest {

    @NotBlank(message = "Observação é obrigatória")
    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;
}
