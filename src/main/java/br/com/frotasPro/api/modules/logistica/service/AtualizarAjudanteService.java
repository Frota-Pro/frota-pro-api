package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.AjudanteRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.AjudanteResponse;
import br.com.frotasPro.api.modules.logistica.domain.Ajudante;
import br.com.frotasPro.api.modules.logistica.mapper.AjudanteMapper;
import br.com.frotasPro.api.modules.logistica.repository.AjudanteRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.modules.logistica.mapper.AjudanteMapper.toResponse;

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
