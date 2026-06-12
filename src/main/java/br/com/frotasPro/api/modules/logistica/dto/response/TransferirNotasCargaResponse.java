package br.com.frotasPro.api.modules.logistica.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransferirNotasCargaResponse {

    private int totalNotasTransferidas;
    private CargaResponse cargaOrigem;
    private CargaResponse cargaDestino;
}
