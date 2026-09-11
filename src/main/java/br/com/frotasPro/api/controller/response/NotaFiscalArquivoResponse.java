package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Vincula uma nota fiscal (cliente + número) ao XML que a originou, quando
 * cadastrada na mão via upload em vez de sincronizada do WinThor — só
 * aparece aqui quando existe um arquivo associado.
 */
@Getter
@Setter
@Builder
public class NotaFiscalArquivoResponse {
    private String cliente;
    private String nota;
    private UUID arquivoId;
    private String nomeArquivo;
    private String urlDownload;
}
