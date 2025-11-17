package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.request.MotoristaRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional
    public MotoristaResponse atualizar(String codigo, MotoristaRequest request) {

        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));

        motorista.setNome(request.getNome().trim().toUpperCase());
        motorista.setEmail(request.getEmail().trim().toLowerCase());
        motorista.setDataNascimento(request.getDataNascimento());
        motorista.setCnh(request.getCnh());
        motorista.setValidadeCnh(request.getValidadeCnh());

        motoristaRepository.save(motorista);

        return MotoristaMapper.toResponse(motorista);
    }
}
