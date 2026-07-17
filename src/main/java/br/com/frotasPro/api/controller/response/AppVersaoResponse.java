package br.com.frotasPro.api.controller.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppVersaoResponse {

    private String versaoNome;
    private String notas;
    private boolean obrigatoria;
    private Long tamanhoBytes;
    private LocalDateTime publicadoEm;
    private String urlDownload;
}
