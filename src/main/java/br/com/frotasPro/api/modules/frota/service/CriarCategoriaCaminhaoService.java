package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.modules.frota.dto.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.modules.frota.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarCategoriaCaminhaoService {

    private final CategoriaCaminhaoRepository repository;

    @Transactional
    public CategoriaCaminhaoResponse criar(CategoriaCaminhaoRequest request) {

        if (repository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("Já existe categoria com o código: " + request.getCodigo());
        }

        CategoriaCaminhao entity = CategoriaCaminhaoMapper.toEntity(request);
        entity = repository.save(entity);

        return CategoriaCaminhaoMapper.toResponse(entity);
    }
}
