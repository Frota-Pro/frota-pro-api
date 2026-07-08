package br.com.frotasPro.api.modules.manutencao.mapper;

import br.com.frotasPro.api.modules.manutencao.dto.response.OficinaResponse;
import br.com.frotasPro.api.modules.manutencao.domain.Oficina;

public class OficinaMapper {

    public static OficinaResponse toResponse(Oficina oficina) {
        return OficinaResponse.builder()
                .id(oficina.getId())
                .nome(oficina.getNome())
                .codigo(oficina.getCodigo())
                .build();
    }
}

