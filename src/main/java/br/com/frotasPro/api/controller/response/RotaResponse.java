package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RotaResponse {

    private UUID id;
    private String codigo;
    private String cidadeInicio;
    private List<String> cidades;
    private int quantidadeDeDias;
}
