package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.MetaProgressoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarMetaPorIdService {

    private final MetaRepository metaRepository;
    private final MetaProgressoService metaProgressoService;

    @Cacheable("meta_buscar_id")
    public MetaResponse buscarPorId(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Meta não encontrada para o id: " + id));

        BigDecimal valorRealizado = metaProgressoService.calcularValorRealizado(meta, null, null);
        BigDecimal percentual = metaProgressoService.calcularPercentual(valorRealizado, meta.getValorMeta());
        Boolean metaAtingida = metaProgressoService.metaAtingida(meta.getTipoMeta(), valorRealizado, meta.getValorMeta());

        return MetaMapper.toResponse(meta, valorRealizado, percentual, metaAtingida);
    }
}
