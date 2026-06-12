package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaCategoriaDesempenhoResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.frota.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.frota.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarDesempenhoMetaCategoriaService {

    private final CategoriaCaminhaoRepository categoriaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaRepository metaRepository;
    private final MetaProgressoService metaProgressoService;
    private final MetaResultadoService metaResultadoService;

    @Transactional
    public MetaCategoriaDesempenhoResponse buscar(String categoriaCodigo, LocalDate dataReferencia) {
        return buscar(categoriaCodigo, dataReferencia, dataReferencia, dataReferencia);
    }

    @Transactional
    public MetaCategoriaDesempenhoResponse buscarPorPeriodo(String categoriaCodigo, LocalDate inicio, LocalDate fim) {
        return buscar(categoriaCodigo, null, inicio, fim);
    }

    private MetaCategoriaDesempenhoResponse buscar(String categoriaCodigo, LocalDate dataReferencia, LocalDate inicio, LocalDate fim) {
        CategoriaCaminhao categoria = categoriaRepository.findByCodigo(categoriaCodigo.trim().toUpperCase())
                .orElseThrow(() -> new ObjectNotFound("Categoria de caminhão não encontrada: " + categoriaCodigo));

        List<Meta> metas = metaRepository.findByCategoriaIdAndStatusMetaInAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                categoria.getId(),
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                fim,
                inicio
        );

        List<Caminhao> caminhoes = caminhaoRepository.findByCategoriaIdAndAtivoTrue(categoria.getId());
        List<MetaCategoriaDesempenhoResponse.Linha> linhas = new ArrayList<>();

        for (Meta meta : metas) {
            for (Caminhao caminhao : caminhoes) {
                var realizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null, inicio, fim);
                var percentual = metaProgressoService.calcularPercentual(realizado, meta.getValorMeta());
                var metaAtingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), realizado, meta.getValorMeta());

                metaResultadoService.registrar(meta, caminhao, realizado, percentual, metaAtingida, inicio, fim);

                linhas.add(MetaCategoriaDesempenhoResponse.Linha.builder()
                        .metaId(meta.getId())
                        .tipoMeta(meta.getTipoMeta())
                        .regraAtingimento(meta.getTipoMeta().getRegraAtingimento())
                        .valorMeta(meta.getValorMeta())
                        .unidade(meta.getUnidade())
                        .caminhaoCodigo(caminhao.getCodigo())
                        .caminhaoDescricao(caminhao.getDescricao())
                        .valorRealizado(realizado)
                        .percentual(percentual)
                        .metaAtingida(metaAtingida)
                        .build());
            }
        }

        linhas.sort(Comparator
                .comparing(MetaCategoriaDesempenhoResponse.Linha::getTipoMeta)
                .thenComparing(MetaCategoriaDesempenhoResponse.Linha::getCaminhaoCodigo));

        return MetaCategoriaDesempenhoResponse.builder()
                .categoriaCodigo(categoria.getCodigo())
                .categoriaDescricao(categoria.getDescricao())
                .dataReferencia(dataReferencia)
                .periodoInicio(inicio)
                .periodoFim(fim)
                .linhas(linhas)
                .build();
    }
}
