package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnviarNotaFiscalEmailRequest {

    @NotBlank(message = "Informe o e-mail do destinatário")
    @Email(message = "E-mail inválido")
    private String destinatario;
}
