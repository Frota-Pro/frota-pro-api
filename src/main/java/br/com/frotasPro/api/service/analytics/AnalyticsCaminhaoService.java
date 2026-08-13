package br.com.frotasPro.api.service.analytics;

import br.com.frotasPro.api.controller.response.AnalyticsCaminhaoResponse;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ManutencaoRepository;
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
 * Analytics de um caminhão específico: km rodado, consumo e custo de
 * manutenção no período, comparado à média de consumo da frota. Usado pela
 * aba "Por Caminhão" da página de Analytics.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsCaminhaoService {

    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public AnalyticsCaminhaoResponse gerar(String codigo, LocalDate inicio, LocalDate fim) {
        PeriodoValidator.obrigatorio(inicio, fim, "período");

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado: " + codigo));

        List<Carga> cargas = cargaRepository.findByCaminhao_CodigoAndStatusCargaAndDtChegadaBetween(
                caminhao.getCodigo(), Status.FINALIZADA, inicio, fim);
        long totalKmRodado = cargas.stream().mapToLong(c -> nvl(c.calcularKmTotal())).sum();

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);
        List<Abastecimento> abastecimentos = abastecimentoRepository
                .findAllByCaminhao_CodigoAndDtAbastecimentoBetween(caminhao.getCodigo(), inicioDt, fimDt);

        BigDecimal totalLitros = abastecimentos.stream()
                .map(Abastecimento::getQtLitros).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCustoCombustivel = abastecimentos.stream()
                .map(Abastecimento::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mediaKmPorLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(totalKmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Manutencao> manutencoes = manutencaoRepository
                .findAllByCaminhaoCodigoAndDataInicioManutencaoBetween(caminhao.getCodigo(), inicio, fim);
        BigDecimal totalCustoManutencao = manutencoes.stream()
                .map(Manutencao::getValor).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Comparação — média de km/L da frota toda no mesmo período.
        List<Abastecimento> abastecimentosFrota = abastecimentoRepository.findAllByDtAbastecimentoBetween(inicioDt, fimDt);
        BigDecimal totalLitrosFrota = abastecimentosFrota.stream()
                .map(Abastecimento::getQtLitros).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Carga> cargasFrota = cargaRepository.findByStatusCargaAndDtChegadaBetween(Status.FINALIZADA, inicio, fim);
        long kmFrota = cargasFrota.stream().mapToLong(c -> nvl(c.calcularKmTotal())).sum();
        BigDecimal mediaKmPorLitroFrota = totalLitrosFrota.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(kmFrota).divide(totalLitrosFrota, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<AnalyticsCaminhaoResponse.PontoSemanal> serieSemanal = montarSerieSemanal(cargas, abastecimentos);

        return AnalyticsCaminhaoResponse.builder()
                .codigoCaminhao(caminhao.getCodigo())
                .placaCaminhao(caminhao.getPlaca())
                .descricaoCaminhao(caminhao.getDescricao())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalCargas((long) cargas.size())
                .totalKmRodado(totalKmRodado)
                .totalLitros(totalLitros)
                .totalCustoCombustivel(totalCustoCombustivel)
                .mediaKmPorLitro(mediaKmPorLitro)
                .totalCustoManutencao(totalCustoManutencao)
                .qtdManutencoes((long) manutencoes.size())
                .mediaKmPorLitroFrota(mediaKmPorLitroFrota)
                .serieSemanal(serieSemanal)
                .build();
    }

    private List<AnalyticsCaminhaoResponse.PontoSemanal> montarSerieSemanal(List<Carga> cargas, List<Abastecimento> abastecimentos) {
        TreeMap<LocalDate, Acumulador> semanas = new TreeMap<>();

        for (Carga c : cargas) {
            if (c.getDtChegada() == null) continue;
            Acumulador acc = semanas.computeIfAbsent(c.getDtChegada().with(DayOfWeek.MONDAY), k -> new Acumulador());
            acc.kmRodado += nvl(c.calcularKmTotal());
        }
        for (Abastecimento a : abastecimentos) {
            if (a.getDtAbastecimento() == null) continue;
            Acumulador acc = semanas.computeIfAbsent(a.getDtAbastecimento().toLocalDate().with(DayOfWeek.MONDAY), k -> new Acumulador());
            if (a.getQtLitros() != null) acc.litros = acc.litros.add(a.getQtLitros());
            if (a.getValorTotal() != null) acc.custoCombustivel = acc.custoCombustivel.add(a.getValorTotal());
        }

        List<AnalyticsCaminhaoResponse.PontoSemanal> serie = new ArrayList<>();
        semanas.forEach((inicioSemana, acc) -> serie.add(
                AnalyticsCaminhaoResponse.PontoSemanal.builder()
                        .inicioSemana(inicioSemana)
                        .kmRodado(acc.kmRodado)
                        .litros(acc.litros)
                        .custoCombustivel(acc.custoCombustivel)
                        .build()
        ));
        return serie;
    }

    private long nvl(Integer v) {
        return v == null ? 0L : v;
    }

    private static class Acumulador {
        long kmRodado = 0;
        BigDecimal litros = BigDecimal.ZERO;
        BigDecimal custoCombustivel = BigDecimal.ZERO;
    }
}
