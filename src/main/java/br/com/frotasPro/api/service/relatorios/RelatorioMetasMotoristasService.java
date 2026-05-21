package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.RelatorioMetasMotoristasResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioMetasMotoristasService {

    private final MotoristaRepository motoristaRepository;
    private final MetaRepository metaRepository;
    private final CargaRepository cargaRepository;
    private final AbastecimentoRepository abastecimentoRepository;

    @Transactional(readOnly = true)
    public RelatorioMetasMotoristasResponse gerar(LocalDate inicio, LocalDate fim, TipoMeta tipoMeta) {
        PeriodoValidator.obrigatorio(inicio, fim, "dtChegada");

        TipoMeta tipo = tipoMeta != null ? tipoMeta : TipoMeta.TONELADA;
        List<Motorista> motoristas = motoristaRepository.findByAtivoTrueOrderByNomeAsc();
        List<UUID> motoristaIds = motoristas.stream().map(Motorista::getId).toList();

        Map<UUID, Meta> metasPorMotorista = buscarMetasPorMotorista(motoristaIds, inicio, fim, tipo);
        Map<UUID, CargaRepository.DesempenhoMotoristaRow> desempenhoPorMotorista =
                buscarDesempenhoPorMotorista(inicio, fim);

        LocalDateTime inicioPeriodo = inicio.atStartOfDay();
        LocalDateTime fimPeriodo = fim.atTime(23, 59, 59);

        List<RelatorioMetasMotoristasResponse.Linha> linhas = motoristas.stream()
                .map(motorista -> montarLinha(
                        motorista,
                        metasPorMotorista.get(motorista.getId()),
                        desempenhoPorMotorista.get(motorista.getId()),
                        tipo,
                        inicioPeriodo,
                        fimPeriodo
                ))
                .toList();

        long totalDentroMeta = linhas.stream().filter(l -> Boolean.TRUE.equals(l.getDentroMeta())).count();

        return RelatorioMetasMotoristasResponse.builder()
                .periodoInicio(inicio)
                .periodoFim(fim)
                .tipoMeta(tipo)
                .totalMotoristas((long) linhas.size())
                .totalDentroMeta(totalDentroMeta)
                .totalForaMeta(linhas.size() - totalDentroMeta)
                .linhas(linhas)
                .build();
    }

    private Map<UUID, Meta> buscarMetasPorMotorista(List<UUID> motoristaIds,
                                                    LocalDate inicio,
                                                    LocalDate fim,
                                                    TipoMeta tipoMeta) {
        Map<UUID, Meta> metasPorMotorista = new HashMap<>();
        if (motoristaIds.isEmpty()) {
            return metasPorMotorista;
        }

        List<Meta> metas = metaRepository.findMetasMotoristasNoPeriodo(
                tipoMeta,
                StatusMeta.EM_ANDAMENTO,
                inicio,
                fim,
                motoristaIds
        );

        for (Meta meta : metas) {
            metasPorMotorista.putIfAbsent(meta.getMotorista().getId(), meta);
        }

        return metasPorMotorista;
    }

    private Map<UUID, CargaRepository.DesempenhoMotoristaRow> buscarDesempenhoPorMotorista(LocalDate inicio,
                                                                                           LocalDate fim) {
        Map<UUID, CargaRepository.DesempenhoMotoristaRow> desempenhoPorMotorista = new HashMap<>();
        List<CargaRepository.DesempenhoMotoristaRow> rows =
                cargaRepository.desempenhoMotoristasNoPeriodo(inicio, fim, Status.FINALIZADA);

        for (CargaRepository.DesempenhoMotoristaRow row : rows) {
            desempenhoPorMotorista.put(row.getMotoristaId(), row);
        }

        return desempenhoPorMotorista;
    }

    private RelatorioMetasMotoristasResponse.Linha montarLinha(Motorista motorista,
                                                               Meta meta,
                                                               CargaRepository.DesempenhoMotoristaRow desempenho,
                                                               TipoMeta tipoMeta,
                                                               LocalDateTime inicio,
                                                               LocalDateTime fim) {
        BigDecimal valorMeta = meta != null ? meta.getValorMeta() : null;
        BigDecimal realizado = calcularRealizado(motorista, desempenho, tipoMeta, inicio, fim);
        BigDecimal percentual = calcularPercentual(realizado, valorMeta);
        boolean dentroMeta = valorMeta != null
                && valorMeta.compareTo(BigDecimal.ZERO) > 0
                && realizado.compareTo(valorMeta) >= 0;

        return RelatorioMetasMotoristasResponse.Linha.builder()
                .codigoMotorista(motorista.getCodigo())
                .nomeMotorista(motorista.getNome())
                .meta(valorMeta)
                .realizado(realizado)
                .percentual(percentual)
                .unidade(meta != null ? meta.getUnidade() : unidadePadrao(tipoMeta))
                .dentroMeta(dentroMeta)
                .status(meta == null ? "SEM META" : dentroMeta ? "DENTRO" : "FORA")
                .build();
    }

    private BigDecimal calcularRealizado(Motorista motorista,
                                         CargaRepository.DesempenhoMotoristaRow desempenho,
                                         TipoMeta tipoMeta,
                                         LocalDateTime inicio,
                                         LocalDateTime fim) {
        if (tipoMeta == TipoMeta.CONSUMO_COMBUSTIVEL) {
            BigDecimal media = abastecimentoRepository.mediaKmLitroPonderadaPorMotoristaEPeriodo(
                    motorista.getId(),
                    inicio,
                    fim
            );
            return media != null ? media : BigDecimal.ZERO;
        }

        if (desempenho == null) {
            return BigDecimal.ZERO;
        }

        return switch (tipoMeta) {
            case QUILOMETRAGEM -> BigDecimal.valueOf(desempenho.getTotalKmRodado() != null
                    ? desempenho.getTotalKmRodado()
                    : 0L);
            case CARGA_TRANSPORTADA -> BigDecimal.valueOf(desempenho.getTotalCargas() != null
                    ? desempenho.getTotalCargas()
                    : 0L);
            case TONELADA -> desempenho.getTotalTonelada() != null
                    ? desempenho.getTotalTonelada()
                    : BigDecimal.ZERO;
            case CONSUMO_COMBUSTIVEL -> BigDecimal.ZERO;
        };
    }

    private BigDecimal calcularPercentual(BigDecimal realizado, BigDecimal meta) {
        if (meta == null || meta.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return realizado.multiply(BigDecimal.valueOf(100)).divide(meta, 2, RoundingMode.HALF_UP);
    }

    private String unidadePadrao(TipoMeta tipoMeta) {
        return switch (tipoMeta) {
            case QUILOMETRAGEM -> "km";
            case CONSUMO_COMBUSTIVEL -> "km/L";
            case TONELADA -> "t";
            case CARGA_TRANSPORTADA -> "cargas";
        };
    }
}
