package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.mapper.ManutencaoMapper;
import br.com.frotasPro.api.repository.ManutencaoRepository;
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
        return manutencaoRepository.findByOficinaCodigoAndDataInicioManutencaoBetween(
                        codigoOficina, inicio, fim, pageable)
                .map(ManutencaoMapper::toResponse);
    }
}
