package br.com.frotasPro.api.service.analytics;

import br.com.frotasPro.api.controller.response.AnalyticsMotoristaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
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
 * Analytics de um motorista específico: desempenho no período (cargas, km,
 * pontualidade, consumo) comparado à média da frota. Usado pela aba "Por
 * Motorista" da página de Analytics.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsMotoristaService {

    private final MotoristaRepository motoristaRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional(readOnly = true)
    public AnalyticsMotoristaResponse gerar(String codigo, LocalDate inicio, LocalDate fim) {
        PeriodoValidator.obrigatorio(inicio, fim, "período");

        Motorista motorista = motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno(codigo)
                .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado: " + codigo));

        List<Carga> cargas = cargaRepository.findByMotorista_CodigoAndStatusCargaAndDtChegadaBetween(
                motorista.getCodigo(), Status.FINALIZADA, inicio, fim);

        long totalKmRodado = cargas.stream().mapToLong(c -> nvl(c.calcularKmTotal())).sum();
        BigDecimal totalTonelada = cargas.stream()
                .map(Carga::getPesoCarga).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValor = cargas.stream()
                .map(Carga::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        long cargasNoPrazo = cargas.stream().filter(c -> c.calcularAtraso() == 0).count();
        double mediaAtraso = cargas.stream().mapToLong(Carga::calcularAtraso).average().orElse(0);
        BigDecimal percentualNoPrazo = cargas.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(cargasNoPrazo * 100.0 / cargas.size()).setScale(1, RoundingMode.HALF_UP);

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);
        List<br.com.frotasPro.api.domain.Abastecimento> abastecimentos =
                abastecimentoRepository.findAllByMotorista_CodigoAndDtAbastecimentoBetween(motorista.getCodigo(), inicioDt, fimDt);

        BigDecimal totalLitros = abastecimentos.stream()
                .map(br.com.frotasPro.api.domain.Abastecimento::getQtLitros).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mediaKmPorLitro = totalLitros.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(totalKmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Comparação com a frota toda no mesmo período.
        List<Carga> cargasFrota = cargaRepository.findByStatusCargaAndDtChegadaBetween(Status.FINALIZADA, inicio, fim);
        long kmFrota = cargasFrota.stream().mapToLong(c -> nvl(c.calcularKmTotal())).sum();
        BigDecimal mediaKmPorCargaFrota = cargasFrota.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((double) kmFrota / cargasFrota.size()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal mediaKmPorCargaMotorista = cargas.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf((double) totalKmRodado / cargas.size()).setScale(1, RoundingMode.HALF_UP);

        List<AnalyticsMotoristaResponse.PontoSemanal> serieSemanal = montarSerieSemanal(cargas);

        return AnalyticsMotoristaResponse.builder()
                .codigoMotorista(motorista.getCodigo())
                .nomeMotorista(motorista.getNome())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalCargas((long) cargas.size())
                .totalKmRodado(totalKmRodado)
                .totalTonelada(totalTonelada)
                .totalValorCargas(totalValor)
                .cargasNoPrazo(cargasNoPrazo)
                .mediaDiasAtrasoChegada(BigDecimal.valueOf(mediaAtraso).setScale(1, RoundingMode.HALF_UP))
                .percentualCargasNoPrazo(percentualNoPrazo)
                .totalLitros(totalLitros)
                .mediaKmPorLitro(mediaKmPorLitro)
                .mediaKmPorCargaFrota(mediaKmPorCargaFrota)
                .mediaKmPorCargaMotorista(mediaKmPorCargaMotorista)
                .serieSemanal(serieSemanal)
                .build();
    }

    private List<AnalyticsMotoristaResponse.PontoSemanal> montarSerieSemanal(List<Carga> cargas) {
        TreeMap<LocalDate, long[]> semanas = new TreeMap<>(); // [cargasFinalizadas, kmRodado]

        for (Carga c : cargas) {
            if (c.getDtChegada() == null) continue;
            LocalDate inicioSemana = c.getDtChegada().with(DayOfWeek.MONDAY);
            long[] acc = semanas.computeIfAbsent(inicioSemana, k -> new long[2]);
            acc[0]++;
            acc[1] += nvl(c.calcularKmTotal());
        }

        List<AnalyticsMotoristaResponse.PontoSemanal> serie = new ArrayList<>();
        semanas.forEach((inicioSemana, acc) -> serie.add(
                AnalyticsMotoristaResponse.PontoSemanal.builder()
                        .inicioSemana(inicioSemana)
                        .cargasFinalizadas(acc[0])
                        .kmRodado(acc[1])
                        .build()
        ));
        return serie;
    }

    private long nvl(Integer v) {
        return v == null ? 0L : v;
    }
}
