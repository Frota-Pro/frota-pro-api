package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferirCaminhaoCargaRequest {

    @NotBlank(message = "Código do caminhão é obrigatório")
    private String codigoCaminhao;
}
