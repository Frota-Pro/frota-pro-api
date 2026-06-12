package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.CaminhaoResponse;
import br.com.frotasPro.api.modules.frota.mapper.CaminhaoMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.service.CaminhaoCachedPage;
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
    public CaminhaoCachedPage listar(Boolean ativo, String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = caminhaoRepository.search(ativo, q, pageable).map(CaminhaoMapper::toResponse);
        return CaminhaoCachedPage.from(resultado);
    }
}
