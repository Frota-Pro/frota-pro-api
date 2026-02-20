package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.excption.BusinessException;
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

        if (meta.getCaminhao() != null || meta.getCategoria() != null || meta.getMotorista() != null) {
            throw new BusinessException("Não é possível excluir uma meta vinculada. Desvincule/cancele a meta antes de excluir.");
        }

        metaRepository.delete(meta);
    }
}
