package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarAbastecimentosService {

    private final AbastecimentoListCacheService cacheService;

    @Transactional(readOnly = true)
    public Page<AbastecimentoResponse> listar(Pageable pageable) {
        var cached = cacheService.listar(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        return cached.toPage(pageable);
    }
}
