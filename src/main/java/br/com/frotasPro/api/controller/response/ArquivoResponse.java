package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ArquivoResponse {

    private UUID id;
    private String nomeOriginal;
    private String urlPreview;
    private String urlDownload;
    private String contentType;
    private Long tamanhoBytes;
}
