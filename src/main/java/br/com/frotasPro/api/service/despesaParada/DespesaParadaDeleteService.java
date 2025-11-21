package br.com.frotasPro.api.service.despesaParada;

import br.com.frotasPro.api.repository.DespesaParadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DespesaParadaDeleteService {

    private final DespesaParadaRepository repository;

    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Despesa não encontrada");
        }
        repository.deleteById(id);
    }
}
