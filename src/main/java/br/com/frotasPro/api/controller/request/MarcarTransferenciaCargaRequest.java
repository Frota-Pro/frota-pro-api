package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarcarTransferenciaCargaRequest {

    @Size(max = 50, message = "Carga destino invalida")
    private String numeroCargaDestino;
}
