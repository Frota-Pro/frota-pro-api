package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarAbastecimentoService {

    private final AbastecimentoRepository repository;

    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Abastecimento não encontrado");
        }
        repository.deleteById(id);
    }
}

