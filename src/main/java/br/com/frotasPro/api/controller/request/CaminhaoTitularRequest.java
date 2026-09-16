package br.com.frotasPro.api.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaminhaoTitularRequest {

    /** Código do novo motorista titular; null/vazio remove o titular atual. */
    private String motoristaTitular;
}
