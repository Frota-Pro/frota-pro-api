package br.com.frotasPro.api.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class ContaRequest {

    private String codigo;
    private String codigoExterno;
    private String nome;
    private UUID grupoContaId;
}
