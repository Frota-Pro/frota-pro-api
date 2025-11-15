package br.com.frotasPro.api.service;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.frotasPro.api.mapper.MotoristaMapper.toResponse;

@Service
@AllArgsConstructor
public class MotoristaService {

    private final MotoristaRepository motoristaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<MotoristaResponse> todosMotoristas(Pageable pageable) {
        Page<Motorista> motoristas = motoristaRepository.findByAtivoTrue(pageable);
        return motoristas.map(MotoristaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MotoristaResponse motoristaPorCodigo(String codigo){
        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));
        return toResponse(motorista);
    }


    @Transactional
    public MotoristaResponse criarMotorista(MotoristaRequest request){
        Motorista motorista = new Motorista();
        copyDtoToEntity(request, motorista);

        motorista = motoristaRepository.save(motorista);

        entityManager.flush();
        entityManager.refresh(motorista);

        return toResponse(motorista);
    }

    @Transactional
    public MotoristaResponse atualizarMotorista(String codigo, MotoristaRequest request) {
        Motorista motorista = motoristaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não Encontrado " + codigo));

        copyDtoToEntity(request, motorista);

        motorista = motoristaRepository.save(motorista);

        return toResponse(motorista);
    }

    @Transactional
    public void deletarMotorista(String codigo) {
        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));

        motorista.setAtivo(false);

        motoristaRepository.save(motorista);
    }

    private void copyDtoToEntity(MotoristaRequest request, Motorista motorista) {

        motorista.setNome(request.getNome().trim().toUpperCase());
        motorista.setEmail(request.getEmail().trim().toLowerCase());
        motorista.setDataNascimento(request.getDataNascimento());
        motorista.setCnh(request.getCnh());
        motorista.setValidadeCnh(request.getValidadeCnh());
    }
}
