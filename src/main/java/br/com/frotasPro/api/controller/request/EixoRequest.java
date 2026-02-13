package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EixoRequest {

    @Min(value = 1, message = "Número do eixo deve ser >= 1")
    private int numero;

    @NotBlank(message = "Código do caminhão é obrigatório")
    @Size(max = 80, message = "Código do caminhão inválido")
    private String codigoCaminhao;
}
