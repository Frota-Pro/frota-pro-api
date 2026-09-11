package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Dados públicos de um CNPJ (Receita Federal), pra pré-preencher o cadastro manual de Cliente. */
@Getter
@Setter
@Builder
public class ConsultaCnpjResponse {
    private String nome;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
}
