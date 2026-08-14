package br.com.frotasPro.api.service.analytics;

import br.com.frotasPro.api.controller.response.AnalyticsAbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.projections.AbastecimentoResumoCaminhao;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
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
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Analytics de abastecimento: evolução de litros/custo no período, e
 * detalhamento por posto e por caminhão — pra achar quem/onde está gastando
 * mais que deveria. Usado pela aba "Abastecimento" da página de Analytics.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsAbastecimentoService {

    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional(readOnly = true)
    public AnalyticsAbastecimentoResponse gerar(LocalDate inicio, LocalDate fim) {
        PeriodoValidator.obrigatorio(inicio, fim, "período");

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);

        List<Abastecimento> abastecimentos = abastecimentoRepository.findAllByDtAbastecimentoBetween(inicioDt, fimDt);

        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getQtLitros).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCusto = abastecimentos.stream()
                .map(Abastecimento::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mediaPrecoLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? totalCusto.divide(totalLitros, 3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<AnalyticsAbastecimentoResponse.PontoSemanal> serieSemanal = montarSerieSemanal(abastecimentos);

        List<AnalyticsAbastecimentoResponse.ResumoPosto> porPosto = abastecimentoRepository
                .resumoPorPostoNoPeriodo(inicioDt, fimDt).stream()
                .map(r -> AnalyticsAbastecimentoResponse.ResumoPosto.builder()
                        .posto(r.getPosto())
                        .totalLitros(r.getTotalLitros())
                        .totalValor(r.getTotalValor())
                        .build())
                .toList();

        List<AnalyticsAbastecimentoResponse.ResumoCaminhao> porCaminhao = abastecimentoRepository
                .resumoPorCaminhaoNoPeriodo(inicioDt, fimDt).stream()
                .sorted((a, b) -> b.getTotalValor().compareTo(a.getTotalValor()))
                .map(this::toResumoCaminhao)
                .toList();

        return AnalyticsAbastecimentoResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalLitros(totalLitros)
                .totalCusto(totalCusto)
                .mediaPrecoLitro(mediaPrecoLitro)
                .serieSemanal(serieSemanal)
                .porPosto(porPosto)
                .porCaminhao(porCaminhao)
                .build();
    }

    private AnalyticsAbastecimentoResponse.ResumoCaminhao toResumoCaminhao(AbastecimentoResumoCaminhao r) {
        return AnalyticsAbastecimentoResponse.ResumoCaminhao.builder()
                .caminhao(r.getCaminhao())
                .totalLitros(r.getTotalLitros())
                .totalValor(r.getTotalValor())
                .mediaKmPorLitro(r.getMediaKmLitro() != null ? r.getMediaKmLitro().setScale(2, RoundingMode.HALF_UP) : null)
                .build();
    }

    private List<AnalyticsAbastecimentoResponse.PontoSemanal> montarSerieSemanal(List<Abastecimento> abastecimentos) {
        TreeMap<LocalDate, BigDecimal[]> semanas = new TreeMap<>(); // [litros, custo]

        for (Abastecimento a : abastecimentos) {
            if (a.getDtAbastecimento() == null) continue;
            LocalDate inicioSemana = a.getDtAbastecimento().toLocalDate().with(DayOfWeek.MONDAY);
            BigDecimal[] acc = semanas.computeIfAbsent(inicioSemana, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (a.getQtLitros() != null) acc[0] = acc[0].add(a.getQtLitros());
            if (a.getValorTotal() != null) acc[1] = acc[1].add(a.getValorTotal());
        }

        List<AnalyticsAbastecimentoResponse.PontoSemanal> serie = new ArrayList<>();
        semanas.forEach((inicioSemana, acc) -> serie.add(
                AnalyticsAbastecimentoResponse.PontoSemanal.builder()
                        .inicioSemana(inicioSemana)
                        .litros(acc[0])
                        .custo(acc[1])
                        .build()
        ));
        return serie;
    }
}
