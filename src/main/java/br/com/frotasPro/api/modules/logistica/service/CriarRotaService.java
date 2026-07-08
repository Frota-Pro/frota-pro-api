package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.RotaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.RotaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Rota;
import br.com.frotasPro.api.modules.logistica.mapper.RotaMapper;
import br.com.frotasPro.api.modules.logistica.repository.RotaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarRotaService {

    private final RotaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public RotaResponse criar(RotaRequest request) {

        Rota rota = RotaMapper.toEntity(request);
        rota = repository.save(rota);

        entityManager.flush();
        entityManager.refresh(rota);

        return RotaMapper.toResponse(rota);
    }
}
