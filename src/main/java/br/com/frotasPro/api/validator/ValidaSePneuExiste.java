package br.com.frotasPro.api.validator;

import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class ValidaSePneuExiste {

    private final PneuRepository pneuRepository;

    public void validar(UUID id) {
        if (!pneuRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND,
                    "Pneu não encontrado");
        }
    }
}
