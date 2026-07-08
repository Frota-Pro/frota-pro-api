package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Motorista;
import br.com.frotasPro.api.modules.logistica.repository.MotoristaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarMotoristaService {

    private final MotoristaRepository motoristaRepository;

    @Transactional
    public void deletar(String codigo) {
        Motorista motorista = motoristaRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ObjectNotFound("ERRO: Motorista não encontrado: " + codigo));

        motorista.setAtivo(false);

        motoristaRepository.save(motorista);
    }
}
