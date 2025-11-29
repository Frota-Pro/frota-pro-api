package br.com.frotasPro.api.service.categoriaCaminhao;

import br.com.frotasPro.api.controller.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarCategoriaCaminhaoService {

    private final CategoriaCaminhaoRepository repository;

    public Page<CategoriaCaminhaoResponse> listar(Pageable pageable) {
        Page<CategoriaCaminhao> page = repository.findAll(pageable);
        return page.map(CategoriaCaminhaoMapper::toResponse);
    }
}
