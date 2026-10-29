package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    /** Identificador do aparelho (o app envia; o web não). Usado no vínculo do motorista com o celular. */
    @Size(max = 100, message = "dispositivoId inválido")
    private String dispositivoId;

    @Size(max = 120, message = "dispositivoNome inválido")
    private String dispositivoNome;
}
