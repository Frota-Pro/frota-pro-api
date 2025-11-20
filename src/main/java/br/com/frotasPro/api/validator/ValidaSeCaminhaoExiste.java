package br.com.frotasPro.api.validator;

import br.com.frotasPro.api.repository.CaminhaoRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@AllArgsConstructor
public class ValidaSeCaminhaoExiste {

    private final CaminhaoRepository caminhaoRepository;

    public void validar(UUID id) {
        if (!caminhaoRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND,
                    "Caminhão não encontrado");
        }
    }
}