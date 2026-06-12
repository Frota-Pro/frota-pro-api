package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.CategoriaCaminhaoResponse;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.modules.frota.mapper.CategoriaCaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
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
