package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.request.MotoristaRequest;
import br.com.frotasPro.api.modules.logistica.dto.response.MotoristaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.mapper.MotoristaMapper;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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

        if (request.getCodigoExterno() != null && !request.getCodigoExterno().trim().isEmpty()) {
            motorista.setCodigoExterno(request.getCodigoExterno().trim());
        } else {
            motorista.setCodigoExterno(null);
        }

        motoristaRepository.save(motorista);

        return MotoristaMapper.toResponse(motorista);
    }
}
