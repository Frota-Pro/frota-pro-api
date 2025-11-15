package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank( message = "campo 'email' é obrigatório")
    private String login;

    @NotBlank( message = "campo 'senha' é obrigatório")
    private String senha;
}
