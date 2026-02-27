package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.service.cache.CachedPage;
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
    public CachedPage<AbastecimentoResponse> listar(int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = repository.findAll(pageable).map(AbastecimentoMapper::toResponse);
        return CachedPage.from(resultado);
    }
}
