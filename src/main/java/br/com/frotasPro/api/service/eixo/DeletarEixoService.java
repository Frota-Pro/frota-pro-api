package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.EixoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarEixoService {

    private final EixoRepository eixoRepository;

    @Transactional
    public void deletar(UUID id) {
        Eixo eixo = eixoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + id));

        eixoRepository.delete(eixo);
    }
}
