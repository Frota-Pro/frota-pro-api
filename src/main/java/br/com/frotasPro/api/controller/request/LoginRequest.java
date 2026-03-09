package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @JsonAlias({"usuario", "username", "email"})
    @NotBlank(message = "campo 'login' é obrigatório")
    private String login;

    @NotBlank(message = "campo 'senha' é obrigatório")
    private String senha;
}
