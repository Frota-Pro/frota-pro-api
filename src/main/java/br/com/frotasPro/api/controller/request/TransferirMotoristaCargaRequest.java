package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferirMotoristaCargaRequest {

    @NotBlank(message = "Código do motorista é obrigatório")
    private String codigoMotorista;
}
