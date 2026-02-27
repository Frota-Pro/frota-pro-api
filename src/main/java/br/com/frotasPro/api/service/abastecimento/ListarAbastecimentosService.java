package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarAbastecimentosService {

    private final AbastecimentoRepository repository;

    @Transactional(readOnly = true)
    @Cacheable("abastecimento_listar")
    public Page<AbastecimentoResponse> listar(Pageable pageable) {
        Page<Abastecimento> page = repository.findAll(pageable);
        return page.map(AbastecimentoMapper::toResponse);
    }
}
