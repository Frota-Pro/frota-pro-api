package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.domain.Conta;

public class ContaMapper {

    public static ContaResponse toResponse(Conta conta) {
        return ContaResponse.builder()
                .id(conta.getId())
                .codigo(conta.getCodigo())
                .codigoExterno(conta.getCodigoExterno())
                .nome(conta.getNome())
                .grupoContaId(conta.getGrupoConta().getId())
                .build();
    }
}
