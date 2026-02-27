package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ListarCargaService {

    private final CargaListCacheService cacheService;

    @Transactional(readOnly = true)
    public Page<CargaMinResponse> listar(String q, LocalDate inicio, LocalDate fim, Pageable pageable) {

        PeriodoValidator.opcional(inicio, fim, "dtSaida");

        String query = (q == null || q.trim().isEmpty()) ? null : q.trim();

        var cached = cacheService.listar(
                query,
                inicio,
                fim,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        return cached.toPage(pageable);
    }
}
