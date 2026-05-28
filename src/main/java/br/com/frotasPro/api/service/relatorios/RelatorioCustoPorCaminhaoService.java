package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioCustoPorCaminhaoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.MovimentacaoSemCarga;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.repository.MovimentacaoSemCargaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioCustoPorCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final MovimentacaoSemCargaRepository movimentacaoSemCargaRepository;

    public RelatorioCustoPorCaminhaoResponse gerar(String codigoCaminhao, LocalDate inicio, LocalDate fim) {

        PeriodoValidator.obrigatorio(inicio, fim, "periodo");

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigoCaminhao)
                .orElseThrow(() -> new IllegalArgumentException("Caminhão não encontrado: " + codigoCaminhao));

        List<RelatorioCustoPorCaminhaoResponse.Linha> linhas = new ArrayList<>();

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(LocalTime.MAX);

        List<Abastecimento> abs = abastecimentoRepository.findByCaminhaoAndPeriodo(
                caminhao.getId(), inicioDt, fimDt
        );

        for (Abastecimento a : abs) {
            linhas.add(RelatorioCustoPorCaminhaoResponse.Linha.builder()
                    .data(LocalDate.from(a.getDtAbastecimento()))
                    .tipo("ABASTECIMENTO")
                    .descricao((a.getPosto() == null ? "Posto" : a.getPosto()) + " - " + (a.getCidade() == null ? "" : a.getCidade()))
                    .valor(nvl(a.getValorTotal()))
                    .build());
        }

        List<Manutencao> mans = manutencaoRepository.findByCaminhaoAndPeriodo(caminhao.getId(), inicio, fim);
        for (Manutencao m : mans) {
            linhas.add(RelatorioCustoPorCaminhaoResponse.Linha.builder()
                    .data(m.getDataFimManutencao())
                    .tipo("MANUTENCAO")
                    .descricao(m.getDescricao())
                    .valor(nvl(m.getValor()))
                    .build());
        }

        List<MovimentacaoSemCarga> movimentacoesSemCarga =
                movimentacaoSemCargaRepository.findByPeriodoComFiltro(inicio, fim, caminhao.getCodigo());
        for (MovimentacaoSemCarga m : movimentacoesSemCarga) {
            linhas.add(RelatorioCustoPorCaminhaoResponse.Linha.builder()
                    .data(m.getDataMovimentacao())
                    .tipo("KM_SEM_CARGA")
                    .descricao("Carga inicial " + m.getCargaInicio().getNumeroCarga()
                            + " | " + m.getKmRodado() + " km sem carga"
                            + " | odômetro " + m.getKmOrigem() + " -> " + m.getKmDestino())
                    .valor(nvl(m.getCustoEstimado()))
                    .build());
        }

        linhas.sort(Comparator.comparing(RelatorioCustoPorCaminhaoResponse.Linha::getData, Comparator.nullsLast(Comparator.naturalOrder())));

        BigDecimal totalComb = abs.stream().map(Abastecimento::getValorTotal).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMan = mans.stream().map(Manutencao::getValor).map(this::nvl).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalKmSemCarga = movimentacoesSemCarga.stream()
                .map(MovimentacaoSemCarga::getCustoEstimado)
                .map(this::nvl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RelatorioCustoPorCaminhaoResponse.builder()
                .codigoCaminhao(caminhao.getCodigo())
                .placaCaminhao(caminhao.getPlaca())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalCombustivel(totalComb)
                .totalManutencao(totalMan)
                .totalKmSemCarga(totalKmSemCarga)
                .totalGeral(totalComb.add(totalMan).add(totalKmSemCarga))
                .linhas(linhas)
                .build();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
