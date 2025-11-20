package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;

public class OficinaMapper {

    public static OficinaResponse toResponse(Oficina oficina) {
        return OficinaResponse.builder()
                .id(oficina.getId())
                .nome(oficina.getNome())
                .codigo(oficina.getCodigo())
                .build();
    }
}

