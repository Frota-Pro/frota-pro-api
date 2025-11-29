package br.com.frotasPro.api.service.categoriaCaminhao;

import br.com.frotasPro.api.controller.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
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
