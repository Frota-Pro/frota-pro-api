package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.cache.CachedPage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MotoristaListCacheService {

    private final MotoristaRepository motoristaRepository;

    @Cacheable("motorista_listar")
    public CachedPage<MotoristaResponse> listar(Boolean ativo, String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = motoristaRepository.search(ativo, q, pageable).map(MotoristaMapper::toResponse);
        return CachedPage.from(resultado);
    }
}
