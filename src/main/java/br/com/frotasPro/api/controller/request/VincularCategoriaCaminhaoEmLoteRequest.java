package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VincularCategoriaCaminhaoEmLoteRequest {

    @NotBlank(message = "Categoria é obrigatória")
    @Size(max = 20, message = "Categoria inválida")
    private String categoriaCodigo;

    @NotEmpty(message = "Lista de caminhões é obrigatória")
    private List<
            @NotBlank(message = "Caminhão inválido")
            @Size(max = 80, message = "Caminhão inválido")
                    String
            > caminhoesCodigo;
}
