package br.com.frotasPro.api.modules.logistica.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import br.com.frotasPro.api.modules.arquivo.dto.response.ArquivoResponse;

@Getter
@Setter
public class DocumentoMotoristaResponse {

    private UUID id;
    private String tipoDocumento;
    private String observacao;
    private ArquivoResponse arquivo;
}
