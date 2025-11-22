package br.com.frotasPro.api.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class EixoRequest {

    private int numero;
    private String codigoCaminhao;
}
