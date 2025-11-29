package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;

public class CategoriaCaminhaoMapper {

    public static CategoriaCaminhao toEntity(CategoriaCaminhaoRequest request) {
        if (request == null) return null;

        CategoriaCaminhao entity = new CategoriaCaminhao();
        entity.setCodigo(request.getCodigo());
        entity.setDescricao(request.getDescricao());
        entity.setObservacao(request.getObservacao());
        entity.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);

        return entity;
    }

    public static void updateEntity(CategoriaCaminhao entity, CategoriaCaminhaoRequest request) {
        entity.setCodigo(request.getCodigo());
        entity.setDescricao(request.getDescricao());
        entity.setObservacao(request.getObservacao());
        if (request.getAtivo() != null) {
            entity.setAtivo(request.getAtivo());
        }
    }

    public static CategoriaCaminhaoResponse toResponse(CategoriaCaminhao entity) {
        if (entity == null) return null;

        CategoriaCaminhaoResponse response = new CategoriaCaminhaoResponse();
        response.setId(entity.getId());
        response.setCodigo(entity.getCodigo());
        response.setDescricao(entity.getDescricao());
        response.setObservacao(entity.getObservacao());
        response.setAtivo(entity.isAtivo());
        return response;
    }
}
