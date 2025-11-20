package br.com.frotasPro.api.service.mecanico;

import br.com.frotasPro.api.domain.Mecanico;
import br.com.frotasPro.api.repository.MecanicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeletarMecanicoService {

    private final MecanicoRepository mecanicoRepository;

    public void deletar(UUID id) {

        Mecanico mecanico = mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "mecanico não encontrado"));

        mecanicoRepository.delete(mecanico);
    }
}
