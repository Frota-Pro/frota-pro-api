package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeletarParadaCargaService {

    private final ParadaCargaRepository repository;

    public void deletar(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parada não encontrada"));

        repository.delete(entity);
    }
}

