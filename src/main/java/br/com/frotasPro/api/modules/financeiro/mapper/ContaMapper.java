package br.com.frotasPro.api.modules.financeiro.mapper;

import br.com.frotasPro.api.modules.financeiro.dto.response.ContaResponse;
import br.com.frotasPro.api.modules.financeiro.domain.Conta;

public class ContaMapper {

    public static ContaResponse toResponse(Conta conta) {
        return ContaResponse.builder()
                .id(conta.getId())
                .codigo(conta.getCodigo())
                .codigoExterno(conta.getCodigoExterno())
                .nome(conta.getNome())
                .grupoConta(conta.getGrupoConta().getCodigo())
                .nomeGrupoConta(conta.getGrupoConta().getNome())
                .build();
    }
}
