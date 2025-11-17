package br.com.frotasPro.api.service.ajudante;

import br.com.frotasPro.api.controller.request.AjudanteRequest;
import br.com.frotasPro.api.controller.response.AjudanteResponse;
import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.AjudanteMapper;
import br.com.frotasPro.api.repository.AjudanteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.AjudanteMapper.toResponse;

@Service
@AllArgsConstructor
public class AtualizarAjudanteService {

    private final AjudanteRepository ajudanteRepository;

    @Transactional
    public AjudanteResponse atualizar(String codigo, AjudanteRequest request) {
        Ajudante ajudante = ajudanteRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("Ajudante não encontrado"));

        AjudanteMapper.updateEntity(ajudante, request);
        ajudante = ajudanteRepository.save(ajudante);
        return toResponse(ajudante);
    }
}
