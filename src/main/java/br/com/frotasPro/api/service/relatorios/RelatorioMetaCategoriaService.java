package br.com.frotasPro.api.service.relatorios;

import br.com.frotasPro.api.controller.response.MetaCategoriaDesempenhoResponse;
import br.com.frotasPro.api.controller.response.RelatorioMetaCategoriaResponse;
import br.com.frotasPro.api.service.meta.BuscarDesempenhoMetaCategoriaService;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioMetaCategoriaService {

    private final BuscarDesempenhoMetaCategoriaService desempenhoService;
    private final MetaProgressoService metaProgressoService;

    public RelatorioMetaCategoriaResponse gerar(String codigoCategoria, LocalDate dataReferencia) {
        return montar(desempenhoService.buscar(codigoCategoria, dataReferencia));
    }

    public RelatorioMetaCategoriaResponse gerarPorPeriodo(String codigoCategoria, LocalDate inicio, LocalDate fim) {
        return montar(desempenhoService.buscarPorPeriodo(codigoCategoria, inicio, fim));
    }

    private RelatorioMetaCategoriaResponse montar(MetaCategoriaDesempenhoResponse desempenho) {

        List<RelatorioMetaCategoriaResponse.Linha> linhas = desempenho.getLinhas().stream()
                .map(linha -> RelatorioMetaCategoriaResponse.Linha.builder()
                        .metaId(linha.getMetaId())
                        .tipoMeta(linha.getTipoMeta())
                        .regraAtingimento(linha.getRegraAtingimento())
                        .regraAtingimentoTexto(regraTexto(linha.getRegraAtingimento()))
                        .valorMeta(linha.getValorMeta())
                        .unidade(linha.getUnidade())
                        .caminhaoCodigo(linha.getCaminhaoCodigo())
                        .caminhaoDescricao(linha.getCaminhaoDescricao())
                        .valorRealizado(linha.getValorRealizado())
                        .percentual(linha.getPercentual())
                        .metaAtingida(linha.getMetaAtingida())
                        .status(metaProgressoService.statusDesempenho(linha.getValorRealizado(), linha.getMetaAtingida()))
                        .build())
                .toList();

        long totalDentro = linhas.stream().filter(linha -> Boolean.TRUE.equals(linha.getMetaAtingida())).count();
        long totalLinhas = linhas.size();
        long totalFora = totalLinhas - totalDentro;

        BigDecimal percentualSucesso = totalLinhas == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalDentro)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalLinhas), 2, RoundingMode.HALF_UP);

        return RelatorioMetaCategoriaResponse.builder()
                .categoriaCodigo(desempenho.getCategoriaCodigo())
                .categoriaDescricao(desempenho.getCategoriaDescricao())
                .dataReferencia(desempenho.getDataReferencia())
                .periodoInicio(desempenho.getPeriodoInicio())
                .periodoFim(desempenho.getPeriodoFim())
                .totalLinhas(totalLinhas)
                .totalDentroMeta(totalDentro)
                .totalForaMeta(totalFora)
                .percentualSucesso(percentualSucesso)
                .linhas(linhas)
                .build();
    }

    private String regraTexto(String regra) {
        if ("MENOR_OU_IGUAL".equals(regra)) {
            return "Menor ou igual a meta";
        }
        if ("MAIOR_OU_IGUAL".equals(regra)) {
            return "Maior ou igual a meta";
        }
        return regra;
    }
}
