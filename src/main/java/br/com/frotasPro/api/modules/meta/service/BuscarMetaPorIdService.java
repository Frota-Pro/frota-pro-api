package br.com.frotasPro.api.modules.meta.service;

import br.com.frotasPro.api.modules.meta.dto.response.MetaResponse;
import br.com.frotasPro.api.modules.meta.domain.Meta;
import br.com.frotasPro.api.modules.meta.mapper.MetaMapper;
import br.com.frotasPro.api.modules.meta.repository.MetaRepository;
import br.com.frotasPro.api.modules.meta.service.MetaProgressoService;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
        String statusDesempenho = metaProgressoService.statusDesempenho(valorRealizado, metaAtingida);

        return MetaMapper.toResponse(meta, valorRealizado, percentual, metaAtingida, statusDesempenho);
    }
}
