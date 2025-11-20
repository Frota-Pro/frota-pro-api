package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.GrupoContaMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BuscarGrupoContaPorIdService {

    private final GrupoContaRepository repository;

    public GrupoContaResponse buscarPorId(UUID id) {

        GrupoConta grupo = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Grupo conta não encontrada"));

        return toResponse(grupo);
    }
}
