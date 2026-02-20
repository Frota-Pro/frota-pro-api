package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public List<MetaResponse> buscar(String codigoCaminhao, LocalDate dataReferencia) {

        Caminhao caminhao = caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigoCaminhao)
                .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado para o código: " + codigoCaminhao));

        List<Meta> metasCaminhao = metaRepository
                .findByCaminhaoCodigoAndStatusMetaAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                        caminhao.getCodigo(),
                        StatusMeta.EM_ANDAMENTO,
                        dataReferencia,
                        dataReferencia
                );

        List<Meta> metasCategoria = new ArrayList<>();
        if (caminhao.getCategoria() != null) {
            metasCategoria = metaRepository
                    .findByCategoriaCodigoAndStatusMetaAndDataIncioLessThanEqualAndDataFimGreaterThanEqual(
                            caminhao.getCategoria().getCodigo(),
                            StatusMeta.EM_ANDAMENTO,
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
            throw new ObjectNotFound("Nenhuma meta ativa para o caminhão " + codigoCaminhao + " na data " + dataReferencia);
        }

        return escolhidas.values().stream()
                .sorted(Comparator.comparing(Meta::getTipoMeta))
                .map(meta -> {
                    var valorRealizado = metaProgressoService.calcularValorRealizado(meta, caminhao, null);
                    if (valorRealizado != null) {
                        meta.setValorRealizado(valorRealizado);
                    }
                    return MetaMapper.toResponse(meta);
                })
                .toList();
    }

}
