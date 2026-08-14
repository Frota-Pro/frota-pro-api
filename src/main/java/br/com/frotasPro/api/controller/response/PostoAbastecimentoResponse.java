package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PostoAbastecimentoResponse {

    private UUID id;
    private String codigo;
    private String nome;
    private String cnpj;
    private String cidade;
    private String uf;
    private String endereco;
    private String observacao;
    private boolean ativo;
}
