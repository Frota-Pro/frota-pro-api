package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarMetaPorIdService {

    private final MetaRepository metaRepository;

    public MetaResponse buscarPorId(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Meta não encontrada para o id: " + id));

        return MetaMapper.toResponse(meta);
    }
}
