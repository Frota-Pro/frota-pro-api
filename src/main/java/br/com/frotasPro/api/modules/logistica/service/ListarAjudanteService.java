package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.AjudanteResponse;
import br.com.frotasPro.api.modules.logistica.mapper.AjudanteMapper;
import br.com.frotasPro.api.modules.logistica.repository.AjudanteRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ListarAjudanteService {

    private final AjudanteRepository ajudanteRepository;


    @Transactional(readOnly = true)
    public Page<AjudanteResponse> listar(Pageable pageable) {
        return ajudanteRepository.findByAtivoTrue(pageable)
                .map(AjudanteMapper::toResponse);
    }
}
