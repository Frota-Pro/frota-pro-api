package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
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

    public Page<ManutencaoResponse> buscarPorCaminhao(
            String codigoCaminhao,
            Pageable pageable
    ) {
        return manutencaoRepository.findByCaminhaoCodigo(codigoCaminhao, pageable)
                .map(ManutencaoMapper::toResponse);
    }

    public Page<ManutencaoResponse> buscarPorCaminhaoEPeriodo(
            String codigoCaminhao,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dataInicioManutencao");

        return manutencaoRepository.findByCaminhaoCodigoAndDataInicioManutencaoBetween(
                        codigoCaminhao, inicio, fim, pageable)
                .map(ManutencaoMapper::toResponse);
    }
}
