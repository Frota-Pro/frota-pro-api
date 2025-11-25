package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MecanicoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String oficina;
}
