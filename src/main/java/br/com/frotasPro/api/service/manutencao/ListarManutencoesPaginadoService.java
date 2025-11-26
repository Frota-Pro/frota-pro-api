package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListarManutencoesPaginadoService {

    private final ManutencaoRepository manutencaoRepository;

    public Page<ManutencaoResponse> listar(Pageable pageable) {
        return manutencaoRepository.findAll(pageable)
                .map(ManutencaoMapper::toResponse);
    }
}
