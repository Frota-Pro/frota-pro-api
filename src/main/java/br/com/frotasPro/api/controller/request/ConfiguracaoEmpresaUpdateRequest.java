package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguracaoEmpresaUpdateRequest {

    @Size(max = 150, message = "Nome da empresa deve ter no máximo 150 caracteres")
    private String nomeEmpresa;

    @Email(message = "E-mail remetente inválido")
    @Size(max = 150, message = "E-mail remetente deve ter no máximo 150 caracteres")
    private String emailRemetente;

    @Size(max = 200, message = "Assunto deve ter no máximo 200 caracteres")
    private String emailAssunto;

    private String emailCorpoHtml;
}
