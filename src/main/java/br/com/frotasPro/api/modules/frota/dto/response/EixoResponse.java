package br.com.frotasPro.api.modules.frota.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class EixoResponse {

    private UUID id;
    private int numero;
    private String codigoCaminhao;
    private String caminhao;
}
