package br.com.frotasPro.api.service.categoriaCaminhao;

import br.com.frotasPro.api.controller.request.CategoriaCaminhaoRequest;
import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
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
