package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** O que a confirmação mudou na carga (antes -> depois). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenciaConfirmadaResponse {

    private String numeroCarga;
    private String numeroCargaExterno;
    private String motoristaAntes;
    private String motoristaDepois;
    private String caminhaoAntes;
    private String caminhaoDepois;
}
