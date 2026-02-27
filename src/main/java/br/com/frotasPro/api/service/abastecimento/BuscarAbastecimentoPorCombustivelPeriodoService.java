package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorCombustivelPeriodoService {

    private final AbastecimentoRepository repository;

    public Page<AbastecimentoResponse> buscar(
            TipoCombustivel tipoCombustivel,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        Page<Abastecimento> page = repository
                .findByTipoCombustivelAndDtAbastecimentoBetween(tipoCombustivel, inicio, fim, pageable);

        return page.map(AbastecimentoMapper::toResponse);
    }
}
