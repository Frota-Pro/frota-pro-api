package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.repository.EixoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class DeletarEixoService {

    private final EixoRepository eixoRepository;

    public void deletar(UUID id) {

        if (!eixoRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Eixo não encontrado");
        }

        eixoRepository.deleteById(id);
    }
}
