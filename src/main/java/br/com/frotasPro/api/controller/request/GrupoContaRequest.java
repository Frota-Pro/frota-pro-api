package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoContaRequest {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 30, message = "Código deve ter no máximo 30 caracteres")
    private String codigo;

    @Size(max = 50, message = "Código externo deve ter no máximo 50 caracteres")
    private String codigoExterno;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
    private String nome;

    @NotBlank(message = "Caminhão é obrigatório")
    @Size(max = 80, message = "Caminhão inválido")
    private String Codigocaminhao;
}
