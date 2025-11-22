package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@Builder
public class GrupoContaResponse {

    private UUID id;
    private String codigo;
    private String codigoExterno;
    private String nome;
    private String codigoCaminhao;
    private String caminhao;
}
