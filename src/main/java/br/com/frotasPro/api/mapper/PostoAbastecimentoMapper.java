package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.PostoAbastecimentoRequest;
import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.domain.PostoAbastecimento;

public class PostoAbastecimentoMapper {

    public static PostoAbastecimento toEntity(PostoAbastecimentoRequest request) {
        if (request == null) return null;

        PostoAbastecimento entity = new PostoAbastecimento();
        entity.setCodigo(request.getCodigo());
        entity.setNome(request.getNome());
        entity.setCnpj(request.getCnpj());
        entity.setCidade(request.getCidade());
        entity.setUf(request.getUf() != null ? request.getUf().toUpperCase() : null);
        entity.setEndereco(request.getEndereco());
        entity.setObservacao(request.getObservacao());
        entity.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);

        return entity;
    }

    public static void updateEntity(PostoAbastecimento entity, PostoAbastecimentoRequest request) {
        entity.setCodigo(request.getCodigo());
        entity.setNome(request.getNome());
        entity.setCnpj(request.getCnpj());
        entity.setCidade(request.getCidade());
        entity.setUf(request.getUf() != null ? request.getUf().toUpperCase() : null);
        entity.setEndereco(request.getEndereco());
        entity.setObservacao(request.getObservacao());
        if (request.getAtivo() != null) {
            entity.setAtivo(request.getAtivo());
        }
    }

    public static PostoAbastecimentoResponse toResponse(PostoAbastecimento entity) {
        if (entity == null) return null;

        PostoAbastecimentoResponse response = new PostoAbastecimentoResponse();
        response.setId(entity.getId());
        response.setCodigo(entity.getCodigo());
        response.setNome(entity.getNome());
        response.setCnpj(entity.getCnpj());
        response.setCidade(entity.getCidade());
        response.setUf(entity.getUf());
        response.setEndereco(entity.getEndereco());
        response.setObservacao(entity.getObservacao());
        response.setAtivo(entity.isAtivo());
        return response;
    }
}
