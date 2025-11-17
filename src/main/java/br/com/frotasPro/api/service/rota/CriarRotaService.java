package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.request.RotaRequest;
import br.com.frotasPro.api.controller.response.RotaResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.mapper.RotaMapper;
import br.com.frotasPro.api.repository.RotaRepository;
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
