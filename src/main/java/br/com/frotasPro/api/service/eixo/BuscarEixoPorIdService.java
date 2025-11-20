package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.repository.EixoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.EixoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class BuscarEixoPorIdService {

    private final EixoRepository eixoRepository;

    public EixoResponse buscar(UUID id) {

        Eixo eixo = eixoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Eixo não encontrado"));

        return toResponse(eixo);
    }
}
