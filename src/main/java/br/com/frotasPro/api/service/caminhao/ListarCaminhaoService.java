package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ListarCaminhaoService {

    private final CaminhaoListCacheService cacheService;

    @Transactional(readOnly = true)
    public Page<CaminhaoResponse> listar(Boolean ativo, String q, Pageable pageable) {
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
