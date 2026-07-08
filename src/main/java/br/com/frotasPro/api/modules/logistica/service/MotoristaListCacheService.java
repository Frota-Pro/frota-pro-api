package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.MotoristaResponse;
import br.com.frotasPro.api.modules.logistica.mapper.MotoristaMapper;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.modules.logistica.service.MotoristaCachedPage;
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
    public MotoristaCachedPage listar(Boolean ativo, String q, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = motoristaRepository.search(ativo, q, pageable).map(MotoristaMapper::toResponse);
        return MotoristaCachedPage.from(resultado);
    }
}
