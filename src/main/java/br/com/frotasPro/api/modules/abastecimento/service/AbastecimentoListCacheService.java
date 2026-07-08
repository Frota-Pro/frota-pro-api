package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.modules.abastecimento.service.AbastecimentoCachedPage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbastecimentoListCacheService {

    private final AbastecimentoRepository repository;

    @Cacheable("abastecimento_listar")
    public AbastecimentoCachedPage listar(int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = repository.findAll(pageable).map(AbastecimentoMapper::toResponse);
        return AbastecimentoCachedPage.from(resultado);
    }
}
