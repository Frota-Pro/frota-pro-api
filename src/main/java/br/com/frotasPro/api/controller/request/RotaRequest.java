package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RotaRequest {

    @NotBlank
    @Size(max = 150)
    private String cidadeInicio;

    @NotEmpty
    private List<@Size(max = 150) String> cidades;

    private int quantidadeDeDias;
}
