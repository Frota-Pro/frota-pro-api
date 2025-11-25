package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorPeriodoService {

    private final AbastecimentoRepository repository;

    public Page<AbastecimentoResponse> buscar(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {
        Page<Abastecimento> page = repository
                .findByDtAbastecimentoBetween(inicio, fim, pageable);

        return page.map(AbastecimentoMapper::toResponse);
    }
}
