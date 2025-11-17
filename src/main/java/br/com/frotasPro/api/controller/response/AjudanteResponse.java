package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AjudanteResponse {

    private UUID id;
    private String codigo;
    private String codigoExterno;
    private String nome;
    private Status status;
    private boolean ativo;
}
