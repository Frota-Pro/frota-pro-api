package br.com.frotasPro.api.modules.abastecimento.service;

import br.com.frotasPro.api.modules.abastecimento.dto.response.AbastecimentoResumoCaminhaoResponse;
import br.com.frotasPro.api.modules.abastecimento.projections.AbastecimentoResumoCaminhao;
import br.com.frotasPro.api.modules.abastecimento.repository.AbastecimentoRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumoAbastecimentoPorCaminhaoService {

    private final AbastecimentoRepository repository;

    @Cacheable("abastecimento_relatorio_resumo")
    public List<AbastecimentoResumoCaminhaoResponse> gerar(
            LocalDate inicio,
            LocalDate fim
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtAbastecimento");

        LocalDateTime ini = inicio.atStartOfDay();
        LocalDateTime end = fim.atTime(23, 59, 59);

        List<AbastecimentoResumoCaminhao> dados =
                repository.resumoPorCaminhaoNoPeriodo(ini, end);

        return dados.stream()
                .map(e -> AbastecimentoResumoCaminhaoResponse.builder()
                        .caminhao(e.getCaminhao())
                        .totalLitros(e.getTotalLitros())
                        .totalValor(e.getTotalValor())
                        .mediaKmLitro(e.getMediaKmLitro())
                        .build()
                ).toList();
    }
}
