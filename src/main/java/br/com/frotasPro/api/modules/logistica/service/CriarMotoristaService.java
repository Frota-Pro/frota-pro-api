package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.MotoristaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.MotoristaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.mapper.MotoristaMapper;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
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

        if (request.getCodigoExterno() != null && !request.getCodigoExterno().trim().isEmpty()) {
            motorista.setCodigoExterno(request.getCodigoExterno().trim());
        } else {
            motorista.setCodigoExterno(null);
        }
    }
}
