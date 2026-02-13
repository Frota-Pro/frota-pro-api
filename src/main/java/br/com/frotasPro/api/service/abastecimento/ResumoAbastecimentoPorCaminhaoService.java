package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResumoCaminhaoResponse;
import br.com.frotasPro.api.projections.AbastecimentoResumoCaminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumoAbastecimentoPorCaminhaoService {

    private final AbastecimentoRepository repository;

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
                        .build()
                ).toList();
    }
}
