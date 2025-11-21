package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.MetaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class BuscarMetaPorIdService {

    private final MetaRepository metaRepository;

    public MetaResponse buscar(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meta não encontrada"));

        return toResponse(meta);
    }
}
