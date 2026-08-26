package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.util.MetaProgressoService;

import br.com.frotasPro.api.controller.response.DashboardMetasResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.MetaResultado;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MetaResultadoRepository;
import br.com.frotasPro.api.service.meta.MetaResultadoService;
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
    private final CaminhaoRepository caminhaoRepository;
    private final MetaProgressoService metaProgressoService;
    private final MetaResultadoService metaResultadoService;

    @Transactional
    public DashboardMetasResponse executar() {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();

        // "Categorias com pior desempenho" e "Top caminhões dentro da meta"
        // leem de tb_meta_resultado — mas até aqui essa tabela só era
        // preenchida como efeito colateral de alguém abrir a tela de
        // progresso de UM caminhão específico (BuscarMetaAtivaComProgressoService).
        // Se ninguém tivesse aberto essa tela ainda, os dois cards do
        // Dashboard ficavam vazios pra sempre, mesmo com metas ativas de
        // verdade. Recalcula aqui na hora pra não depender de ninguém ter
        // visitado outra tela antes.
        recalcularResultadosMetasAtivas();

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

    private void recalcularResultadosMetasAtivas() {
        for (Meta meta : metaRepository.findByStatusMeta(StatusMeta.EM_ANDAMENTO)) {
            for (Caminhao caminhao : caminhoesAlvoDaMeta(meta)) {
                BigDecimal valorRealizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null);
                BigDecimal percentual = metaProgressoService.calcularPercentual(valorRealizado, meta.getValorMeta());
                Boolean atingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), valorRealizado, meta.getValorMeta());
                metaResultadoService.registrar(meta, caminhao, valorRealizado, percentual, atingida);
            }
        }
    }

    /** Meta ligada direto a um caminhão vale só pra ele; ligada a uma categoria vale pra todos os caminhões ativos dela. */
    private List<Caminhao> caminhoesAlvoDaMeta(Meta meta) {
        if (meta.getCaminhao() != null) {
            return List.of(meta.getCaminhao());
        }
        if (meta.getCategoria() != null) {
            return caminhaoRepository.findByCategoriaIdAndAtivoTrue(meta.getCategoria().getId());
        }
        return List.of();
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
