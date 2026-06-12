package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.modules.frota.dto.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.modules.frota.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarCategoriaCaminhaoService {

    private final CategoriaCaminhaoRepository repository;

    @Transactional
    public CategoriaCaminhaoResponse atualizar(UUID id, CategoriaCaminhaoRequest request) {

        CategoriaCaminhao entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada para o id: " + id));

        CategoriaCaminhaoMapper.updateEntity(entity, request);

        entity = repository.save(entity);

        return CategoriaCaminhaoMapper.toResponse(entity);
    }
}
