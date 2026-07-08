package br.com.frotasPro.api.modules.relatorio.service;

import br.com.frotasPro.api.modules.meta.dto.response.RelatorioRankingMotoristasResponse;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioRankingMotoristasService {

    private final CargaRepository cargaRepository;

    public RelatorioRankingMotoristasResponse gerar(LocalDate inicio, LocalDate fim) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtChegada");

        List<CargaRepository.RankingMotoristaRow> rows = cargaRepository.rankingMotoristas(inicio, fim);

        List<RelatorioRankingMotoristasResponse.Linha> linhas = new ArrayList<>();
        long pos = 1;

        for (CargaRepository.RankingMotoristaRow r : rows) {
            linhas.add(RelatorioRankingMotoristasResponse.Linha.builder()
                    .posicao(pos++)
                    .codigoMotorista(r.getCodigoMotorista())
                    .nomeMotorista(r.getNomeMotorista())
                    .totalCargas(r.getTotalCargas())
                    .totalTonelada(r.getTotalTonelada())
                    .totalKmRodado(r.getTotalKmRodado())
                    .totalValorCargas(r.getTotalValorCargas())
                    .build());
        }

        return RelatorioRankingMotoristasResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalMotoristas((long) linhas.size())
                .linhas(linhas)
                .build();
    }
}
