package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.util.MetaProgressoService;

import br.com.frotasPro.api.controller.response.DashboardMetasResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.MetaResultado;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        // Uma meta de categoria e uma meta direta do mesmo tipo (ex.: Tonelada)
        // podem coexistir pro mesmo caminhão, mas só a direta vale pra ele —
        // mesma prioridade que a tela de progresso do caminhão já aplica.
        // Sem isso, o caminhão aparecia "fora" por causa de uma meta de
        // categoria que nem é mais a dele naquele tipo.
        List<MetaResultado> resultados = aplicarPrioridadeDiretaSobreCategoria(
                metaResultadoRepository.findResultadosAtuaisMetasEmAndamento()
        );

        return DashboardMetasResponse.builder()
                .metasAtivas(metasAtivas)
                .metasVencendo(metasVencendo)
                .caminhoesForaMeta(contarCaminhoesForaMeta(resultados))
                .categoriasPiorDesempenho(categoriasPiorDesempenho(resultados))
                .topCaminhoesDentroMeta(topCaminhoesDentroMeta(resultados))
                .build();
    }

    /**
     * Se um caminhão tem, pro mesmo tipo de meta (ex.: Tonelada), um
     * resultado vindo de uma meta DIRETA dele e outro vindo de uma meta de
     * CATEGORIA, descarta o de categoria — a direta tem prioridade (mesma
     * regra da tela de progresso do caminhão, ver BuscarMetaAtivaComProgressoService).
     * Sem isso, os dois contavam ao mesmo tempo pro caminhão.
     */
    private List<MetaResultado> aplicarPrioridadeDiretaSobreCategoria(List<MetaResultado> resultados) {
        Map<UUID, Map<TipoMeta, MetaResultado>> escolhidoPorCaminhaoETipo = new HashMap<>();

        for (MetaResultado r : resultados) {
            if (r.getCaminhao() == null) continue;

            Map<TipoMeta, MetaResultado> porTipo = escolhidoPorCaminhaoETipo
                    .computeIfAbsent(r.getCaminhao().getId(), k -> new HashMap<>());
            TipoMeta tipo = r.getMeta().getTipoMeta();
            MetaResultado existente = porTipo.get(tipo);

            if (existente == null) {
                porTipo.put(tipo, r);
                continue;
            }

            boolean novaEhDireta = r.getMeta().getCaminhao() != null;
            boolean existenteEhDireta = existente.getMeta().getCaminhao() != null;
            if (novaEhDireta && !existenteEhDireta) {
                porTipo.put(tipo, r);
            }
        }

        return escolhidoPorCaminhaoETipo.values().stream()
                .flatMap(porTipo -> porTipo.values().stream())
                .toList();
    }

    private long contarCaminhoesForaMeta(List<MetaResultado> resultados) {
        return agruparPorCaminhao(resultados).values().stream()
                .filter(rs -> rs.stream().anyMatch(r -> !r.isMetaAtingida()))
                .count();
    }

    private List<DashboardMetasResponse.CategoriaResumo> categoriasPiorDesempenho(List<MetaResultado> resultados) {
        // Agrupa pela categoria do CAMINHÃO (Caminhao.categoria), não da meta —
        // a maioria das metas reais é por caminhão ou por motorista, não por
        // categoria, então agrupar por meta.getCategoria() deixava de fora
        // quase todo mundo e a categoria que sobrava aparecia com 0% (parecia
        // "sem problema" mesmo com caminhões fora da meta de verdade em outras
        // categorias). Toda categoria de caminhão que tiver algum resultado
        // entra na conta, não só as com meta configurada diretamente nela.
        Map<CategoriaCaminhao, List<MetaResultado>> porCategoria = resultados.stream()
                .filter(r -> r.getCaminhao() != null && r.getCaminhao().getCategoria() != null)
                .collect(Collectors.groupingBy(r -> r.getCaminhao().getCategoria()));

        return porCategoria.entrySet().stream()
                .map(entry -> {
                    // Cada caminhão conta só uma vez na categoria, mesmo tendo
                    // várias metas ativas de tipos diferentes ao mesmo tempo —
                    // "fora da meta" aqui é "fora de pelo menos uma das metas
                    // dele", não "número de checagens caminhão×meta que falharam".
                    Map<UUID, List<MetaResultado>> porCaminhao = agruparPorCaminhao(entry.getValue());

                    long total = porCaminhao.size();
                    long fora = porCaminhao.values().stream()
                            .filter(rs -> rs.stream().anyMatch(r -> !r.isMetaAtingida()))
                            .count();
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

    private Map<UUID, List<MetaResultado>> agruparPorCaminhao(List<MetaResultado> resultados) {
        return resultados.stream().collect(Collectors.groupingBy(r -> r.getCaminhao().getId()));
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

    private List<DashboardMetasResponse.CaminhaoResumo> topCaminhoesDentroMeta(List<MetaResultado> resultados) {
        return resultados.stream()
                .filter(MetaResultado::isMetaAtingida)
                .sorted(Comparator.comparing(MetaResultado::getPercentual, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
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
