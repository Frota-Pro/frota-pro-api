package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MecanicoResponse {

    private UUID id;
    private String nome;
    private String codigo;
    private UUID oficinaId;
}
