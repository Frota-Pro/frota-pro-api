package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeletarMetaService {

    private final MetaRepository metaRepository;

    public void deletar(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Meta não encontrada"));

        metaRepository.delete(meta);
    }
}

