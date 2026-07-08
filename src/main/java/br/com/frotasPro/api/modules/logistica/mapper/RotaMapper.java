package br.com.frotasPro.api.modules.logistica.mapper;

import br.com.frotasPro.api.modules.logistica.dto.request.RotaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.RotaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Rota;

public class RotaMapper {

    public static Rota toEntity(RotaRequest request) {
        Rota rota = new Rota();
        rota.setCidadeInicio(request.getCidadeInicio());
        rota.setCidades(request.getCidades());
        rota.setQuantidadeDeDias(request.getQuantidadeDeDias());
        return rota;
    }

    public static void updateEntity(Rota rota, RotaRequest request) {
        rota.setCidadeInicio(request.getCidadeInicio());
        rota.setCidades(request.getCidades());
        rota.setQuantidadeDeDias(request.getQuantidadeDeDias());
    }

    public static RotaResponse toResponse(Rota rota) {
        return RotaResponse.builder()
                .id(rota.getId())
                .codigo(rota.getCodigo())
                .cidadeInicio(rota.getCidadeInicio())
                .cidades(rota.getCidades())
                .quantidadeDeDias(rota.getQuantidadeDeDias())
                .build();
    }
}
