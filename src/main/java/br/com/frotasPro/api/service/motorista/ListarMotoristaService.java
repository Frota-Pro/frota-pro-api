package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.MotoristaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarMotoristaService {

    private final MotoristaListCacheService cacheService;

    @Transactional(readOnly = true)
    public Page<MotoristaResponse> listar(Boolean ativo, String q, Pageable pageable) {
        var cached = cacheService.listar(
                ativo,
                q,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        return cached.toPage(pageable);
    }
}
