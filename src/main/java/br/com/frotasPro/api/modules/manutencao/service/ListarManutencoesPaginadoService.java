package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.ManutencaoResponse;
import br.com.frotasPro.api.modules.manutencao.mapper.ManutencaoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.ManutencaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ListarManutencoesPaginadoService {

    private final ManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public Page<ManutencaoResponse> listar(Pageable pageable) {
        return manutencaoRepository.findAll(pageable)
                .map(ManutencaoMapper::toResponse);
    }
}
