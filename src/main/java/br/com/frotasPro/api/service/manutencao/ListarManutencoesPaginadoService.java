package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ListarManutencoesPaginadoService {

    private final ManutencaoRepository manutencaoRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional(readOnly = true)
    public Page<ManutencaoResponse> listar(Pageable pageable) {
        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        return manutencaoRepository.findAll(pageable)
                .map(m -> ManutencaoMapper.toResponse(m, integracaoAtiva));
    }
}
