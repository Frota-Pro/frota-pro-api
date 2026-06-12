package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.EixoResponse;
import br.com.frotasPro.api.modules.frota.domain.Eixo;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.modules.frota.mapper.EixoMapper;
import br.com.frotasPro.api.modules.frota.repository.EixoRepository;
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
                .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + id));
        return EixoMapper.toResponse(eixo);
    }
}
