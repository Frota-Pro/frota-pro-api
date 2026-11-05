package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinhaTransporteRequest {

    @NotBlank(message = "Nome da linha é obrigatório")
    @Size(min = 2, max = 60, message = "Nome da linha deve ter entre 2 e 60 caracteres")
    private String nome;

    /** Opcional: nulo mantém o valor atual (na criação a linha nasce ativa). */
    private Boolean ativo;
}
