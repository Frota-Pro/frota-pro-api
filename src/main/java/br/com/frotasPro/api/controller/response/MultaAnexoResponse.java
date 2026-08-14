package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MultaAnexoResponse {

    private UUID id;
    private String tipoAnexo;
    private ArquivoResponse arquivo;
}
