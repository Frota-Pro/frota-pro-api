package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.controller.response.DashboardMetasResponse;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.MetaResultado;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MetaResultadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuscarDashboardMetasService {

    private final MetaRepository metaRepository;
    private final MetaResultadoRepository metaResultadoRepository;

    @Transactional(readOnly = true)
    public DashboardMetasResponse executar() {
        LocalDate hoje = LocalDate.now();
        long metasAtivas = metaRepository.countByStatusMetaAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                StatusMeta.EM_ANDAMENTO,
                hoje,
                hoje
        );
        long metasVencendo = metaRepository.countByStatusMetaInAndDataFimBetween(
                List.of(StatusMeta.EM_ANDAMENTO),
                hoje,
                hoje.plusDays(7)
        );
        long caminhoesFora = metaResultadoRepository.countCaminhoesForaMetaAtual();
        List<MetaResultado> resultados = metaResultadoRepository.findResultadosAtuaisMetasEmAndamento();

        return DashboardMetasResponse.builder()
                .metasAtivas(metasAtivas)
                .metasVencendo(metasVencendo)
                .caminhoesForaMeta(caminhoesFora)
                .categoriasPiorDesempenho(categoriasPiorDesempenho(resultados))
                .topCaminhoesDentroMeta(topCaminhoesDentroMeta())
                .build();
    }

    private List<DashboardMetasResponse.CategoriaResumo> categoriasPiorDesempenho(List<MetaResultado> resultados) {
        Map<CategoriaCaminhao, List<MetaResultado>> porCategoria = resultados.stream()
                .filter(r -> r.getMeta().getCategoria() != null)
                .collect(Collectors.groupingBy(r -> r.getMeta().getCategoria()));

        return porCategoria.entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().size();
                    long fora = entry.getValue().stream().filter(r -> !r.isMetaAtingida()).count();
                    BigDecimal percentualFora = total == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(fora)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

                    return DashboardMetasResponse.CategoriaResumo.builder()
                            .categoriaCodigo(entry.getKey().getCodigo())
                            .categoriaDescricao(entry.getKey().getDescricao())
                            .totalResultados(total)
                            .totalForaMeta(fora)
                            .percentualForaMeta(percentualFora)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardMetasResponse.CategoriaResumo::getPercentualForaMeta).reversed())
                .limit(5)
                .toList();
    }

    private List<DashboardMetasResponse.CaminhaoResumo> topCaminhoesDentroMeta() {
        return metaResultadoRepository.findTopResultadosDentroMeta().stream()
                .limit(5)
                .map(resultado -> DashboardMetasResponse.CaminhaoResumo.builder()
                        .caminhaoCodigo(resultado.getCaminhao().getCodigo())
                        .caminhaoDescricao(resultado.getCaminhao().getDescricao())
                        .tipoMeta(resultado.getMeta().getTipoMeta())
                        .valorMeta(resultado.getMeta().getValorMeta())
                        .valorRealizado(resultado.getValorRealizado())
                        .percentual(resultado.getPercentual())
                        .build())
                .toList();
    }
}
