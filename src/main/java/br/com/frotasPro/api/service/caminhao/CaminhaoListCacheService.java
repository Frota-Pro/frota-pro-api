package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.controller.response.CaminhaoResponse;
import br.com.frotasPro.api.mapper.CaminhaoMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.service.cache.CachedPage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaminhaoListCacheService {

    private final CaminhaoRepository caminhaoRepository;

    @Cacheable("caminhao_listar")
    public CachedPage<CaminhaoResponse> listar(Boolean ativo, String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = caminhaoRepository.search(ativo, q, pageable).map(CaminhaoMapper::toResponse);
        return CachedPage.from(resultado);
    }
}
