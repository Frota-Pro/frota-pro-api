package br.com.frotasPro.api.service.ajudante;

import br.com.frotasPro.api.controller.request.AjudanteRequest;
import br.com.frotasPro.api.controller.response.AjudanteResponse;
import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.mapper.AjudanteMapper;
import br.com.frotasPro.api.repository.AjudanteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.AjudanteMapper.toResponse;

@Service
@AllArgsConstructor
public class CriarAjudanteService {

    private final AjudanteRepository ajudanteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public AjudanteResponse criar(AjudanteRequest request) {
        Ajudante ajudante = AjudanteMapper.toEntity(request);
        ajudante = ajudanteRepository.save(ajudante);

        entityManager.flush();
        entityManager.refresh(ajudante);

        return toResponse(ajudante);
    }
}
