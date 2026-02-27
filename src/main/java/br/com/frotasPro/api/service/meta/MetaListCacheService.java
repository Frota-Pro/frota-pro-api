package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.service.cache.MetaCachedPage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetaListCacheService {

    private final MetaRepository metaRepository;

    @Cacheable("meta_listar")
    public MetaCachedPage listar(int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = metaRepository.findAll(pageable).map(MetaMapper::toResponse);
        return MetaCachedPage.from(resultado);
    }
}
