package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
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
        Page<Abastecimento> page = repository
                .findByFormaPagamentoAndDtAbastecimentoBetween(formaPagamento, inicio, fim, pageable);

        return page.map(AbastecimentoMapper::toResponse);
    }
}
