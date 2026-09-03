package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequest {

    @NotBlank(message = "CNPJ/CPF é obrigatório")
    @Size(max = 20, message = "CNPJ/CPF inválido")
    private String documento;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome inválido")
    private String nome;

    @Size(max = 200)
    private String logradouro;

    @Size(max = 20)
    private String numero;

    @Size(max = 100)
    private String complemento;

    @Size(max = 100)
    private String bairro;

    @Size(max = 150)
    private String cidade;

    @Size(max = 2)
    private String uf;

    @Size(max = 9)
    private String cep;

    @Size(max = 20)
    private String telefone;

    @Size(max = 150)
    private String email;
}
