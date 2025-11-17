package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MotoristaResponse criar(MotoristaRequest request) {
        Motorista motorista = new Motorista();
        copyDtoToEntity(request, motorista);

        motorista = motoristaRepository.save(motorista);

        entityManager.flush();
        entityManager.refresh(motorista);

        return MotoristaMapper.toResponse(motorista);
    }

    private void copyDtoToEntity(MotoristaRequest request, Motorista motorista) {
        motorista.setNome(request.getNome().trim().toUpperCase());
        motorista.setEmail(request.getEmail().trim().toLowerCase());
        motorista.setDataNascimento(request.getDataNascimento());
        motorista.setCnh(request.getCnh());
        motorista.setValidadeCnh(request.getValidadeCnh());
    }
}
