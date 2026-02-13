package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentosFiltradoService {

    private final AbastecimentoRepository repository;

    public Page<AbastecimentoResponse> buscar(
            String q,
            String caminhao,
            String motorista,
            TipoCombustivel tipo,
            FormaPagamento forma,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    ) {

        PeriodoValidator.opcional(inicio, fim, "dtAbastecimento");

        String qN = norm(q);
        String caminhaoN = norm(caminhao);
        String motoristaN = norm(motorista);


        Pageable pageableNoSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return repository.filtrarNative(
                        qN,
                        caminhaoN,
                        motoristaN,
                        tipo != null ? tipo.name() : null,
                        forma != null ? forma.name() : null,
                        inicio,
                        fim,
                        pageableNoSort
                )
                .map(AbastecimentoMapper::toResponse);
    }

    private String norm(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
