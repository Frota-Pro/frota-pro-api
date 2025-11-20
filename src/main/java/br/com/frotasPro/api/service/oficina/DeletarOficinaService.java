package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletarOficinaService {

    private final OficinaRepository repository;

    public void deletar(UUID id) {

        Oficina oficina = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oficina não encontrada!"));

        repository.delete(oficina);
    }
}
