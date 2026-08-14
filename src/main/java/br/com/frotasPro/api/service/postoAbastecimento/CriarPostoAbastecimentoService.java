package br.com.frotasPro.api.service.postoAbastecimento;

import br.com.frotasPro.api.controller.request.PostoAbastecimentoRequest;
import br.com.frotasPro.api.controller.response.PostoAbastecimentoResponse;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.mapper.PostoAbastecimentoMapper;
import br.com.frotasPro.api.repository.PostoAbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarPostoAbastecimentoService {

    private final PostoAbastecimentoRepository repository;

    @Transactional
    public PostoAbastecimentoResponse criar(PostoAbastecimentoRequest request) {

        if (repository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("Já existe posto com o código: " + request.getCodigo());
        }

        PostoAbastecimento entity = PostoAbastecimentoMapper.toEntity(request);
        entity = repository.save(entity);

        return PostoAbastecimentoMapper.toResponse(entity);
    }
}
