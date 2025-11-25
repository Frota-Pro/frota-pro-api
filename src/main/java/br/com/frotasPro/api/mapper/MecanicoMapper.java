package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MecanicoResponse;
import br.com.frotasPro.api.domain.Mecanico;

public class MecanicoMapper {

    public static MecanicoResponse toResponse(Mecanico mecanico) {

        String codigoOficina = null;
        String nomeOficina = null;

        if (mecanico.getOficina() != null) {
            codigoOficina = mecanico.getOficina().getCodigo();
            nomeOficina = mecanico.getOficina().getNome();
        }

        return MecanicoResponse.builder()
                .id(mecanico.getId())
                .nome(mecanico.getNome())
                .codigo(mecanico.getCodigo())
                .codigoOficina(codigoOficina)
                .oficina(nomeOficina)
                .build();
    }
}
