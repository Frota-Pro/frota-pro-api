package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.meta.dto.response.MetaResponse;
import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.mapper.MetaMapper;
import br.com.frotasPro.api.modules.frota.repository.CaminhaoRepository;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.modules.meta.service.MetaProgressoService;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import br.com.frotasPro.api.shared.enums.TipoMeta;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuscarMetaAtivaComProgressoService {

    private final MetaRepository metaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final MetaProgressoService metaProgressoService;
    private final MetaResultadoService metaResultadoService;

    @Cacheable(value = "meta_ativas_caminhao", key = "#codigoCaminhao + '|' + #dataReferencia")
    @Transactional
    public List<MetaResponse> buscar(String codigoCaminhao, LocalDate dataReferencia) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigoCaminhao)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + codigoCaminhao));

        List<Meta> metasCaminhao = metaRepository
                .findByCaminhaoIdAndStatusMetaInAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                        caminhao.getId(),
                        List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                        dataReferencia,
                        dataReferencia
                );

        List<Meta> metasCategoria = new ArrayList<>();
        metasCategoria = metaRepository.findMetasCategoriaVinculadasAoCaminhaoNaData(
                caminhao.getId(),
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                dataReferencia
        );

        // compatibilidade para metas antigas que ainda não possuem vínculo materializado
        if (metasCategoria.isEmpty() && caminhao.getCategoria() != null) {
            metasCategoria = metaRepository.findByCategoriaIdAndStatusMetaInAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                    caminhao.getCategoria().getId(),
                    List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA),
                    dataReferencia,
                    dataReferencia
            );
        }

        Map<TipoMeta, Meta> escolhidas = new LinkedHashMap<>();
        for (Meta meta : metasCaminhao) {
            escolhidas.putIfAbsent(meta.getTipoMeta(), meta);
        }
        for (Meta meta : metasCategoria) {
            escolhidas.putIfAbsent(meta.getTipoMeta(), meta);
        }

        if (escolhidas.isEmpty()) {
            return List.of();
        }

        return escolhidas.values().stream()
                .sorted(Comparator.comparing(Meta::getTipoMeta))
                .map(meta -> {
                    var valorRealizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null);
                    var percentual = metaProgressoService.calcularPercentual(valorRealizado, meta.getValorMeta());
                    var metaAtingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), valorRealizado, meta.getValorMeta());
                    var statusDesempenho = metaProgressoService.statusDesempenho(valorRealizado, metaAtingida);
                    metaResultadoService.registrar(meta, caminhao, valorRealizado, percentual, metaAtingida);
                    return MetaMapper.toResponse(meta, valorRealizado, percentual, metaAtingida, statusDesempenho);
                })
                .toList();
    }

}
