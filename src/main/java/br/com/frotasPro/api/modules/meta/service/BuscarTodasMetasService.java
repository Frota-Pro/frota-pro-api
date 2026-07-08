package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.meta.dto.response.MetaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarTodasMetasService {

    private final MetaListCacheService cacheService;

    public Page<MetaResponse> listar(Pageable pageable) {
        var cached = cacheService.listar(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        return cached.toPage(pageable);
    }
}
