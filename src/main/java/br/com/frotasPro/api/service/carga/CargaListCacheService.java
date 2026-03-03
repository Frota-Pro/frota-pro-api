package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.cache.CargaCachedPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CargaListCacheService {

    private final CargaRepository cargaRepository;

    public CargaCachedPage listar(String q, LocalDate inicio, LocalDate fim, int page, int size, Sort sort) {
        var pageable = PageRequest.of(page, size, sort);
        var resultado = cargaRepository.listarFiltrado(q, inicio, fim, pageable).map(CargaMapper::toMinResponse);
        return CargaCachedPage.from(resultado);
    }
}
