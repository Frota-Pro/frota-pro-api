package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.domain.Abastecimento;
import br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
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

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        Page<Abastecimento> page = repository
                .findByDtAbastecimentoBetween(inicio, fim, pageable);

        return page.map(AbastecimentoMapper::toResponse);
    }
}
