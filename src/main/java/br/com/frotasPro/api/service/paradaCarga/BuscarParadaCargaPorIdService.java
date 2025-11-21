package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ParadaCargaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BuscarParadaCargaPorIdService {

    private final ParadaCargaRepository repository;

    public ParadaCargaResponse buscar(UUID id) {
        ParadaCarga entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parada não encontrada"));

        return toResponse(entity);
    }
}
