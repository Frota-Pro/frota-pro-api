package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarMetaService {

    private final MetaRepository metaRepository;

    @Transactional
    public void deletar(UUID id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Meta não encontrada para o id: " + id));

        metaRepository.delete(meta);
    }
}
