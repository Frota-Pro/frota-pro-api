package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Eixo;

public class EixoMapper {

    public static EixoResponse toResponse(Eixo eixo) {
        return EixoResponse.builder()
                .id(eixo.getId())
                .numero(eixo.getNumero())
                .caminhaoId(eixo.getCaminhao().getId())
                .build();
    }
}
