package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoGastoPorCombustivelResponse;
import br.com.frotasPro.api.modules.abastecimento.projections.AbastecimentoGastoPorCombustivel;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioAbastecimentoService {

    private final AbastecimentoRepository repository;

    @Transactional(readOnly = true)
    @Cacheable("abastecimento_relatorio_gasto")
    public List<AbastecimentoGastoPorCombustivelResponse> gastoPorCombustivel(
            LocalDate inicio,
            LocalDate fim
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);

        List<AbastecimentoGastoPorCombustivel> dados =
                repository.gastoPorTipoCombustivelNoPeriodo(inicioDt, fimDt);

        return dados.stream()
                .map(d -> AbastecimentoGastoPorCombustivelResponse.builder()
                        .tipoCombustivel(d.getTipoCombustivel())
                        .totalLitros(d.getTotalLitros())
                        .totalValor(d.getTotalValor())
                        .build())
                .toList();
    }
}
