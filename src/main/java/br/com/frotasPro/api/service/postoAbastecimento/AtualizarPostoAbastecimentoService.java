package br.com.frotasPro.api.service.postoAbastecimento;

import br.com.frotasPro.api.controller.request.PostoAbastecimentoRequest;
import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.PostoAbastecimentoMapper;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizarPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;

    @Transactional
    public PostoAbastecimentoResponse atualizar(UUID id, PostoAbastecimentoRequest request) {

        PostoAbastecimento entity = repository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Posto de abastecimento não encontrado para o id: " + id));

        PostoAbastecimentoMapper.updateEntity(entity, request);

        entity = repository.save(entity);

        return PostoAbastecimentoMapper.toResponse(entity);
    }
}
