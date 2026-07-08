package br.com.frotasPro.api.modules.logistica.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import br.com.frotasPro.api.shared.enums.Status;

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
