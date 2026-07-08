package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResponse;
import br.com.frotasPro.api.modules.abastecimento.domain.Abastecimento;
import br.com.frotasPro.api.modules.abastecimento.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.shared.enums.FormaPagamento;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorFormaPagamentoPeriodoService {

    private final AbastecimentoRepository repository;

    public Page<AbastecimentoResponse> buscar(
            FormaPagamento formaPagamento,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        Page<Abastecimento> page = repository
                .findByFormaPagamentoAndDtAbastecimentoBetween(formaPagamento, inicio, fim, pageable);

        return page.map(AbastecimentoMapper::toResponse);
    }
}
