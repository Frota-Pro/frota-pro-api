package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BuscarTodasManutencoesService {

    private final ManutencaoRepository manutencaoRepository;

    public List<ManutencaoResponse> buscarTodos() {
        return manutencaoRepository.findAll().stream()
                .map(ManutencaoMapper::toResponse)
                .toList();
    }
}
