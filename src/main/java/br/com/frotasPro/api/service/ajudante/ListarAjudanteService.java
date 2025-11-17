package br.com.frotasPro.api.service.ajudante;

import br.com.frotasPro.api.controller.response.AjudanteResponse;
import br.com.frotasPro.api.mapper.AjudanteMapper;
import br.com.frotasPro.api.repository.AjudanteRepository;
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
