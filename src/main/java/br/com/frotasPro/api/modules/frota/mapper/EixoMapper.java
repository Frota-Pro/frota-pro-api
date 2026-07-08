package br.com.frotasPro.api.modules.frota.mapper;

import br.com.frotasPro.api.modules.frota.dto.response.EixoResponse;
import br.com.frotasPro.api.modules.frota.domain.Eixo;

public class EixoMapper {

    public static EixoResponse toResponse(Eixo eixo) {
        return EixoResponse.builder()
                .id(eixo.getId())
                .numero(eixo.getNumero())
                .codigoCaminhao(eixo.getCaminhao().getCodigo())
                .caminhao(eixo.getCaminhao().getDescricao())
                .build();
    }
}
