package br.com.frotasPro.api.modules.manutencao.service;

import br.com.frotasPro.api.modules.manutencao.dto.response.ManutencaoResponse;
import br.com.frotasPro.api.modules.manutencao.mapper.ManutencaoMapper;
import br.com.frotasPro.api.modules.manutencao.repository.ManutencaoRepository;
import br.com.frotasPro.api.shared.validator.PeriodoValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class BuscarManutencoesPorOficinaEPeriodoService {

    private final ManutencaoRepository manutencaoRepository;

    public Page<ManutencaoResponse> buscar(
            String codigoOficina,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    ) {

        PeriodoValidator.obrigatorio(inicio, fim, "dataInicioManutencao");

        return manutencaoRepository.findByOficinaCodigoAndDataInicioManutencaoBetween(
                        codigoOficina, inicio, fim, pageable)
                .map(ManutencaoMapper::toResponse);
    }
}
