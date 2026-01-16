package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VincularCategoriaCaminhaoEmLoteRequest {

    @NotBlank
    private String categoriaCodigo;

    @NotEmpty
    private List<String> caminhoesCodigo;
}
