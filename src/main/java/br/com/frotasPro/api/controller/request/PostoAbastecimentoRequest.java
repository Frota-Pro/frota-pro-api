package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostoAbastecimentoRequest {

    @NotBlank(message = "Código é obrigatório")
    @Size(max = 20, message = "Código deve ter no máximo 20 caracteres")
    private String codigo;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @Size(max = 20, message = "CNPJ deve ter no máximo 20 caracteres")
    private String cnpj;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF inválida (ex: PB)")
    private String uf;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String endereco;

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    private String observacao;

    private Boolean ativo;
}
