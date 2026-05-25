package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarHistoricoMetaComProgressoService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaProgressoService metaProgressoService;
    private final MetaResultadoService metaResultadoService;

    @Cacheable("meta_historico")
    @Transactional
    public List<MetaResponse> historico(String caminhao, String categoria, String motorista, LocalDate inicio, LocalDate fim) {
        return buscarHistoricoComProgresso(caminhao, categoria, motorista, inicio, fim);
    }

    @Cacheable("meta_historico_caminhao")
    @Transactional
    public List<MetaResponse> historicoPorCaminhao(String codigoCaminhao, LocalDate inicio, LocalDate fim) {
        return buscarHistoricoComProgresso(codigoCaminhao, null, null, inicio, fim);
    }

    private List<MetaResponse> buscarHistoricoComProgresso(
            String caminhao,
            String categoria,
            String motorista,
            LocalDate inicio,
            LocalDate fim
    ) {
        final Caminhao caminhaoRef = (caminhao != null && !caminhao.isBlank())
                ? caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(caminhao)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + caminhao))
                : null;

        List<Meta> metas = metaRepository.historicoMetas(caminhao, categoria, motorista, inicio, fim);
        return metas.stream()
                .map(meta -> {
                    var valorRealizado = meta.getValorRealizado();
                    if (caminhaoRef != null) {
                        valorRealizado = metaProgressoService.calcularValorRealizado(meta, caminhaoRef, null);
                    }
                    var percentual = metaProgressoService.calcularPercentual(valorRealizado, meta.getValorMeta());
                    var metaAtingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), valorRealizado, meta.getValorMeta());
                    var statusDesempenho = metaProgressoService.statusDesempenho(valorRealizado, metaAtingida);
                    if (caminhaoRef != null) {
                        metaResultadoService.registrar(meta, caminhaoRef, valorRealizado, percentual, metaAtingida);
                    }
                    return MetaMapper.toResponse(meta, valorRealizado, percentual, metaAtingida, statusDesempenho);
                })
                .toList();
    }
}
