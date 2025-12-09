package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentoMotoristaResponse {

    private UUID id;
    private String tipoDocumento;
    private String observacao;
    private ArquivoResponse arquivo;
}
