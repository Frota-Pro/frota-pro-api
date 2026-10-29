package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MesclarRotaResponse {

    private String codigoRemovida;
    private String codigoDestino;
    private String nomeDestino;
    private long cargasMovidas;
}
