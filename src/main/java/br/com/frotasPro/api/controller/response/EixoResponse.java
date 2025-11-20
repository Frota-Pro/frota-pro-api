package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EixoResponse {

    private UUID id;
    private int numero;
    private UUID caminhaoId;
}
