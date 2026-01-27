package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AtualizarOrdemEntregaRequest {

    @NotNull
    private List<String> clientes;
}