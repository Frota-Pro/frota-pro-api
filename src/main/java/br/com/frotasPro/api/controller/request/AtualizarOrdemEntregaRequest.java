package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AtualizarOrdemEntregaRequest {

    @NotEmpty(message = "Lista de clientes é obrigatória")
    private List<@NotBlank(message = "Cliente inválido") String> clientes;
}
