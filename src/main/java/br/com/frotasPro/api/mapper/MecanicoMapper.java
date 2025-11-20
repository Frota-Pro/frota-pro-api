package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;

public class MecanicoMapper {

    public static MecanicoResponse toResponse(Mecanico mecanico) {
        return MecanicoResponse.builder()
                .id(mecanico.getId())
                .nome(mecanico.getNome())
                .codigo(mecanico.getCodigo())
                .oficinaId(mecanico.getOficina().getId())
                .build();
    }
}
