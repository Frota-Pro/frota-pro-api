package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class VincularLinhaCaminhaoEmLoteRequest {

    @NotNull(message = "Linha é obrigatória")
    private UUID linhaId;

    @NotEmpty(message = "Lista de caminhões é obrigatória")
    private List<
            @NotBlank(message = "Caminhão inválido")
            @Size(max = 80, message = "Caminhão inválido")
                    String
            > caminhoesCodigo;
}
