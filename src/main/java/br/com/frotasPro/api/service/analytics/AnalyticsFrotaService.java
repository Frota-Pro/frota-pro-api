package br.com.frotasPro.api.service.analytics;

import br.com.frotasPro.api.controller.response.AnalyticsFrotaResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.projections.AbastecimentoResumoCaminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Visão geral da frota pra página de Analytics: totais do período, evolução
 * semanal (pra gráfico) e rankings de melhor/pior motorista e caminhão.
 * Reaproveita as agregações que já existem em CargaRepository/AbastecimentoRepository
 * (as mesmas usadas pelos relatórios de ranking) em vez de duplicar a lógica —
 * só a série semanal é cálculo novo.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsFrotaService {

    private static final int TOP_N = 5;

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional(readOnly = true)
    public AnalyticsFrotaResponse gerar(LocalDate inicio, LocalDate fim) {
        PeriodoValidator.obrigatorio(inicio, fim, "período");

        List<Carga> cargasFinalizadas = cargaRepository.findByStatusCargaAndDtChegadaBetween(Status.FINALIZADA, inicio, fim);

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);
        List<Abastecimento> abastecimentos = abastecimentoRepository.findAllByDtAbastecimentoBetween(inicioDt, fimDt);

        long totalKmRodado = cargasFinalizadas.stream()
                .mapToLong(c -> nvl(c.calcularKmTotal()))
                .sum();

        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getQtLitros)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCusto = abastecimentos.stream()
                .map(Abastecimento::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mediaKmPorLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(totalKmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<AnalyticsFrotaResponse.PontoSemanal> serieSemanal = montarSerieSemanal(cargasFinalizadas, abastecimentos);

        List<CargaRepository.RankingMotoristaRow> rankingMotoristas = cargaRepository.rankingMotoristas(inicio, fim);
        List<AnalyticsFrotaResponse.RankingMotoristaItem> motoristasOrdenados = rankingMotoristas.stream()
                .filter(r -> r.getTotalKmRodado() != null)
                .sorted(Comparator.comparing(CargaRepository.RankingMotoristaRow::getTotalKmRodado).reversed())
                .map(r -> AnalyticsFrotaResponse.RankingMotoristaItem.builder()
                        .codigoMotorista(r.getCodigoMotorista())
                        .nomeMotorista(r.getNomeMotorista())
                        .totalCargas(r.getTotalCargas())
                        .totalKmRodado(r.getTotalKmRodado())
                        .totalTonelada(r.getTotalTonelada())
                        .build())
                .toList();

        List<AbastecimentoResumoCaminhao> resumoCaminhoes = abastecimentoRepository.resumoPorCaminhaoNoPeriodo(inicioDt, fimDt);
        List<AnalyticsFrotaResponse.RankingCaminhaoItem> caminhoesOrdenados = resumoCaminhoes.stream()
                .filter(r -> r.getMediaKmLitro() != null)
                .sorted(Comparator.comparing(AbastecimentoResumoCaminhao::getMediaKmLitro).reversed())
                .map(r -> AnalyticsFrotaResponse.RankingCaminhaoItem.builder()
                        .caminhao(r.getCaminhao())
                        .mediaKmPorLitro(r.getMediaKmLitro().setScale(2, RoundingMode.HALF_UP))
                        .totalLitros(r.getTotalLitros())
                        .build())
                .toList();

        return AnalyticsFrotaResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalCargasFinalizadas((long) cargasFinalizadas.size())
                .totalKmRodado(totalKmRodado)
                .totalLitros(totalLitros)
                .totalCustoCombustivel(totalCusto)
                .mediaKmPorLitro(mediaKmPorLitro)
                .serieSemanal(serieSemanal)
                .topMotoristas(primeirosN(motoristasOrdenados, TOP_N))
                .piorMotoristas(ultimosNInvertido(motoristasOrdenados, TOP_N))
                .topCaminhoesConsumo(primeirosN(caminhoesOrdenados, TOP_N))
                .piorCaminhoesConsumo(ultimosNInvertido(caminhoesOrdenados, TOP_N))
                .build();
    }

    private List<AnalyticsFrotaResponse.PontoSemanal> montarSerieSemanal(List<Carga> cargas, List<Abastecimento> abastecimentos) {
        TreeMap<LocalDate, Acumulador> semanas = new TreeMap<>();

        for (Carga c : cargas) {
            if (c.getDtChegada() == null) continue;
            Acumulador acc = semanas.computeIfAbsent(inicioDaSemana(c.getDtChegada()), k -> new Acumulador());
            acc.cargasFinalizadas++;
            acc.kmRodado += nvl(c.calcularKmTotal());
        }

        for (Abastecimento a : abastecimentos) {
            if (a.getDtAbastecimento() == null) continue;
            Acumulador acc = semanas.computeIfAbsent(inicioDaSemana(a.getDtAbastecimento().toLocalDate()), k -> new Acumulador());
            if (a.getQtLitros() != null) acc.litros = acc.litros.add(a.getQtLitros());
            if (a.getValorTotal() != null) acc.custoCombustivel = acc.custoCombustivel.add(a.getValorTotal());
        }

        List<AnalyticsFrotaResponse.PontoSemanal> serie = new ArrayList<>();
        semanas.forEach((inicioSemana, acc) -> serie.add(
                AnalyticsFrotaResponse.PontoSemanal.builder()
                        .inicioSemana(inicioSemana)
                        .cargasFinalizadas(acc.cargasFinalizadas)
                        .kmRodado(acc.kmRodado)
                        .litros(acc.litros)
                        .custoCombustivel(acc.custoCombustivel)
                        .build()
        ));
        return serie;
    }

    private LocalDate inicioDaSemana(LocalDate data) {
        return data.with(DayOfWeek.MONDAY);
    }

    private long nvl(Integer v) {
        return v == null ? 0L : v;
    }

    private <T> List<T> primeirosN(List<T> lista, int n) {
        return lista.stream().limit(n).toList();
    }

    /** Últimos N da lista (os piores), na ordem "pior primeiro". */
    private <T> List<T> ultimosNInvertido(List<T> lista, int n) {
        List<T> piores = new ArrayList<>(lista.subList(Math.max(0, lista.size() - n), lista.size()));
        java.util.Collections.reverse(piores);
        return piores;
    }

    private static class Acumulador {
        long cargasFinalizadas = 0;
        long kmRodado = 0;
        BigDecimal litros = BigDecimal.ZERO;
        BigDecimal custoCombustivel = BigDecimal.ZERO;
    }
}
