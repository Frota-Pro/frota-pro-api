package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.ManutencaoMapper.toResponse;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class BuscarManutencaoPorIdService {

    private final ManutencaoRepository manutencaoRepository;

    public ManutencaoResponse buscar(UUID id) {
        Manutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Manutenção não encontrada"));
        return toResponse(manutencao);
    }
}
