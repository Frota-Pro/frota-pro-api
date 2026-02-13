package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MotoristaRequest {

    // opcional
    @Size(max = 50, message = "Código externo deve ter no máximo 50 caracteres")
    private String codigoExterno;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotNull(message = "Data de nascimento é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    @NotBlank(message = "CNH é obrigatória")
    @Size(min = 10, max = 20, message = "CNH deve ter entre 10 e 20 caracteres")
    private String cnh;

    @NotNull(message = "Validade da CNH é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate validadeCnh;
}
