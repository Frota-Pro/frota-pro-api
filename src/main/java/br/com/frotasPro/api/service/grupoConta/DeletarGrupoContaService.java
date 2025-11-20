package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.domain.GrupoConta;
import br.com.frotasPro.api.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeletarGrupoContaService {

    private final GrupoContaRepository repository;

    public void deletar(UUID id) {

        repository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Grupo conta não encontrada"));


        repository.deleteById(id);
    }
}
