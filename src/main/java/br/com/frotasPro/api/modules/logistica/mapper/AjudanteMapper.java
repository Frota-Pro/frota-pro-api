package br.com.frotasPro.api.modules.logistica.mapper;

import br.com.frotasPro.api.modules.logistica.dto.request.AjudanteRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.AjudanteResponse;
import br.com.frotasPro.api.modules.logistica.domain.Ajudante;

public class AjudanteMapper {

    public static Ajudante toEntity(AjudanteRequest request) {
        Ajudante ajudante = new Ajudante();
        ajudante.setNome(request.getNome());
        return ajudante;
    }

    public static void updateEntity(Ajudante ajudante, AjudanteRequest request) {
        ajudante.setNome(request.getNome());
    }

    public static AjudanteResponse toResponse(Ajudante ajudante) {
        return AjudanteResponse.builder()
                .id(ajudante.getId())
                .codigo(ajudante.getCodigo())
                .codigoExterno(ajudante.getCodigoExterno())
                .nome(ajudante.getNome())
                .status(ajudante.getStatus())
                .ativo(ajudante.isAtivo())
                .build();
    }
}
