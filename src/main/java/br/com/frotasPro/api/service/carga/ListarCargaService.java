package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ListarCargaService {

    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public Page<CargaMinResponse> listar(String q, LocalDate inicio, LocalDate fim, Pageable pageable) {
        String query = (q == null || q.trim().isEmpty()) ? null : q.trim();

        Page<Carga> page = cargaRepository.listarFiltrado(query, inicio, fim, pageable);

        return page.map(CargaMapper::toMinResponse);
    }
}