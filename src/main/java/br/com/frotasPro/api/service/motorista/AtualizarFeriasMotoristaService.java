package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.controller.request.MotoristaFeriasRequest;
import br.com.frotasPro.api.controller.response.MotoristaResponse;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.MotoristaMapper;
import br.com.frotasPro.api.repository.MotoristaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AtualizarFeriasMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional
    public MotoristaResponse atualizar(String codigo, MotoristaFeriasRequest request) {
        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));

        motorista.setEmFerias(request.isEmFerias());
        if (request.isEmFerias()) {
            motorista.setFeriasInicio(request.getFeriasInicio());
            motorista.setFeriasFimPrevisto(request.getFeriasFimPrevisto());
        } else {
            motorista.setFeriasInicio(null);
            motorista.setFeriasFimPrevisto(null);
        }

        motoristaRepository.save(motorista);
        return MotoristaMapper.toResponse(motorista);
    }
}
