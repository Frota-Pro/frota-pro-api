package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.LinhaRelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.controller.response.RelatorioMetaMensalMotoristaResponse;
import br.com.frotasPro.api.domain.*;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelatorioMetaMensalMotoristaService {

    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;
    private final MetaRepository metaRepository;

    @Transactional(readOnly = true)
    public RelatorioMetaMensalMotoristaResponse gerar(
            String codigoMotorista,
            LocalDate inicio,
            LocalDate fim) {

        PeriodoValidator.obrigatorio(inicio, fim, "dtSaida");

        List<Carga> cargas = cargaRepository
                .findByMotoristaCodigoAndPeriodo(codigoMotorista, inicio, fim);

        if (cargas.isEmpty()) {
            return RelatorioMetaMensalMotoristaResponse.builder()
                    .codigoMotorista(codigoMotorista)
                    .periodoInicio(inicio)
                    .periodoFim(fim)
                    .linhas(new ArrayList<>())
                    .totalKmRodado(0L)
                    .totalTonelada(BigDecimal.ZERO)
                    .totalLitros(BigDecimal.ZERO)
                    .totalValorAbastecimento(BigDecimal.ZERO)
                    .mediaGeralKmPorLitro(BigDecimal.ZERO)
                    .realizadoToneladaPercentual(BigDecimal.ZERO)
                    .build();
        }

        Motorista motorista = cargas.get(0).getMotorista();
        Caminhao caminhao = cargas.get(0).getCaminhao();
        CategoriaCaminhao categoriaCaminhao = caminhao != null ? caminhao.getCategoria() : null;

        BigDecimal objetivoMesTonelada = buscarMetaTonelada(motorista, caminhao, categoriaCaminhao);
        BigDecimal metaConsumo = buscarMetaConsumoCombustivel(motorista, caminhao, categoriaCaminhao);

        List<LinhaRelatorioMetaMensalMotoristaResponse> linhas = new ArrayList<>();

        BigDecimal totalTonelada = BigDecimal.ZERO;
        long totalKmRodado = 0L;
        BigDecimal totalLitros = BigDecimal.ZERO;
        BigDecimal totalValorAbastecimento = BigDecimal.ZERO;

        for (Carga carga : cargas) {

            Integer kmIni = carga.getKmInicial();
            Integer kmFin = carga.getKmFinal();
            long kmRodado = (kmIni != null && kmFin != null) ? kmFin - kmIni : 0L;

            Map<java.util.UUID, Abastecimento> abastecimentosDaViagem = new LinkedHashMap<>();

            if (kmIni != null && kmFin != null) {
                List<Abastecimento> porKm = abastecimentoRepository.findByCaminhaoAndKmRodado(
                        carga.getCaminhao().getId(),
                        kmIni,
                        kmFin
                );
                for (Abastecimento a : porKm) {
                    abastecimentosDaViagem.put(a.getId(), a);
                }
            }

            LocalDate inicioViagem = carga.getDtSaida() != null ? carga.getDtSaida() : inicio;
            LocalDate fimViagem = carga.getDtChegada() != null ? carga.getDtChegada() : inicioViagem;
            LocalDateTime dtInicioViagem = inicioViagem.atStartOfDay();
            LocalDateTime dtFimViagem = fimViagem.atTime(23, 59, 59);

            List<Abastecimento> porPeriodo = abastecimentoRepository.findByCaminhaoAndPeriodo(
                    carga.getCaminhao().getId(),
                    dtInicioViagem,
                    dtFimViagem
            );
            for (Abastecimento a : porPeriodo) {
                abastecimentosDaViagem.put(a.getId(), a);
            }

            List<Abastecimento> abastecimentos = new ArrayList<>(abastecimentosDaViagem.values());

            BigDecimal litros = abastecimentos.stream()
                    .map(Abastecimento::getQtLitros)
                    .filter(l -> l != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valorAbastecimento = abastecimentos.stream()
                    .map(Abastecimento::getValorTotal)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal mediaManual = abastecimentos.stream()
                    .map(Abastecimento::getMediaKmLitro)
                    .filter(m -> m != null)
                    .findFirst()
                    .orElse(null);

            BigDecimal mediaKmLitro = calcularMediaKmLitro(kmRodado, litros, mediaManual, metaConsumo);

            LinhaRelatorioMetaMensalMotoristaResponse linha =
                    LinhaRelatorioMetaMensalMotoristaResponse.builder()
                            .data(carga.getDtSaida())
                            .lote(carga.getNumeroCarga())
                            .cidade(carga.getRota().getCidadeInicio())
                            .valorCarga(carga.getValorTotal())
                            .tonelagem(carga.getPesoCarga())
                            .kmInicial(kmIni)
                            .kmFinal(kmFin)
                            .kmRodado(kmRodado)
                            .litros(litros)
                            .valorAbastecimento(valorAbastecimento)
                            .mediaKmLitro(mediaKmLitro)
                            .build();

            linhas.add(linha);

            if (carga.getPesoCarga() != null) {
                totalTonelada = totalTonelada.add(carga.getPesoCarga());
            }
            totalKmRodado += kmRodado;
            totalLitros = totalLitros.add(litros);
            totalValorAbastecimento = totalValorAbastecimento.add(valorAbastecimento);
        }

        BigDecimal mediaGeralKmPorLitro;
        if (totalLitros.compareTo(BigDecimal.ZERO) > 0) {
            mediaGeralKmPorLitro = BigDecimal.valueOf(totalKmRodado).divide(totalLitros, 2, RoundingMode.HALF_UP);
        } else {
            mediaGeralKmPorLitro = linhas.stream()
                    .map(LinhaRelatorioMetaMensalMotoristaResponse::getMediaKmLitro)
                    .filter(m -> m != null)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }

        BigDecimal realizadoPercentual =
                (objetivoMesTonelada != null && objetivoMesTonelada.compareTo(BigDecimal.ZERO) > 0)
                        ? totalTonelada.multiply(BigDecimal.valueOf(100)).divide(objetivoMesTonelada, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        return RelatorioMetaMensalMotoristaResponse.builder()
                .nomeMotorista(motorista.getNome())
                .codigoMotorista(motorista.getCodigo())
                .placaCaminhao(caminhao != null ? caminhao.getPlaca() : null)
                .codigoCaminhao(caminhao != null ? caminhao.getCodigo() : null)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .objetivoMesTonelada(objetivoMesTonelada)
                .metaConsumoKmPorLitro(metaConsumo)
                .linhas(linhas)
                .totalTonelada(totalTonelada)
                .totalKmRodado(totalKmRodado)
                .totalLitros(totalLitros)
                .totalValorAbastecimento(totalValorAbastecimento)
                .mediaGeralKmPorLitro(mediaGeralKmPorLitro)
                .realizadoToneladaPercentual(realizadoPercentual)
                .build();
    }

    private BigDecimal calcularMediaKmLitro(long kmRodado,
                                            BigDecimal litros,
                                            BigDecimal mediaManual,
                                            BigDecimal metaConsumo) {
        if (litros != null && litros.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.valueOf(kmRodado).divide(litros, 2, RoundingMode.HALF_UP);
        }
        if (mediaManual != null) {
            return mediaManual;
        }
        return metaConsumo;
    }

    private BigDecimal buscarMetaTonelada(Motorista motorista, Caminhao caminhao, CategoriaCaminhao categoriaCaminhao) {

        Optional<Meta> metaOpt = Optional.empty();

        if (motorista != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, motorista, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && caminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.TONELADA, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }

    private BigDecimal buscarMetaConsumoCombustivel(Motorista motorista, Caminhao caminhao, CategoriaCaminhao categoriaCaminhao) {

        Optional<Meta> metaOpt = Optional.empty();

        if (caminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCaminhaoAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, caminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && categoriaCaminhao != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndCategoriaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, categoriaCaminhao, StatusMeta.EM_ANDAMENTO);
        }

        if (metaOpt.isEmpty() && motorista != null) {
            metaOpt = metaRepository.findFirstByTipoMetaAndMotoristaAndStatusMetaOrderByDataIncioDesc(
                    TipoMeta.CONSUMO_COMBUSTIVEL, motorista, StatusMeta.EM_ANDAMENTO);
        }

        return metaOpt.map(Meta::getValorMeta).orElse(null);
    }
}
