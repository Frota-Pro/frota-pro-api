package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ContaResponse {

    private UUID id;
    private String codigo;
    private String codigoExterno;
    private String nome;
    private UUID grupoContaId;
}
