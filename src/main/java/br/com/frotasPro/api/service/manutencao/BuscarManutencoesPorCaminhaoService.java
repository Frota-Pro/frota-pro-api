package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.utils.PeriodoValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class BuscarManutencoesPorCaminhaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    public Page<ManutencaoResponse> buscarPorCaminhao(
            String codigoCaminhao,
            Pageable pageable
    ) {
        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        return manutencaoRepository.findByCaminhaoCodigo(codigoCaminhao, pageable)
                .map(m -> ManutencaoMapper.toResponse(m, integracaoAtiva));
    }

    public Page<ManutencaoResponse> buscarPorCaminhaoEPeriodo(
            String codigoCaminhao,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dataInicioManutencao");

        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        return manutencaoRepository.findByCaminhaoCodigoAndDataInicioManutencaoBetween(
                        codigoCaminhao, inicio, fim, pageable)
                .map(m -> ManutencaoMapper.toResponse(m, integracaoAtiva));
    }
}
