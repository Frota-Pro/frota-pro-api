package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.domain.GrupoConta;

public class GrupoContaMapper {

    public static GrupoContaResponse toResponse(GrupoConta grupo) {
        return GrupoContaResponse.builder()
                .id(grupo.getId())
                .codigo(grupo.getCodigo())
                .codigoExterno(grupo.getCodigoExterno())
                .nome(grupo.getNome())
                .codigoCaminhao(grupo.getCaminhao().getCodigo())
                .caminhao(grupo.getCaminhao().getDescricao())
                .build();
    }
}
