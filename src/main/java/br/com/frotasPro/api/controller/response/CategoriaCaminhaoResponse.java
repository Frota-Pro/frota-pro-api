package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoriaCaminhaoResponse {

    private UUID id;
    private String codigo;
    private String descricao;
    private String observacao;
    private boolean ativo;
}
