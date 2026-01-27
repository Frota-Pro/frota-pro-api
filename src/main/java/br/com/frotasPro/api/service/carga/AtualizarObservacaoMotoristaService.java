package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtualizarObservacaoMotoristaService {

    private final CargaRepository cargaRepository;

    @Transactional
    public void atualizar(String numeroCarga, String observacao) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada: " + numeroCarga));

        carga.setObservacaoMotorista(observacao);

        cargaRepository.save(carga);
    }
}