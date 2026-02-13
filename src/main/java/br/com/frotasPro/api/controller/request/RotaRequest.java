package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RotaRequest {

    @NotBlank(message = "Cidade de início é obrigatória")
    @Size(max = 150, message = "Cidade de início deve ter no máximo 150 caracteres")
    private String cidadeInicio;

    @NotEmpty(message = "Lista de cidades é obrigatória")
    private List<
            @NotBlank(message = "Cidade inválida")
            @Size(max = 150, message = "Cidade deve ter no máximo 150 caracteres")
                    String
            > cidades;

    @Min(value = 0, message = "Quantidade de dias deve ser >= 0")
    private int quantidadeDeDias;
}
