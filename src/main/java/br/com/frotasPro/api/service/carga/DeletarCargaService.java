package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletarCargaService {

    private final CargaRepository cargaRepository;

    @Transactional
    public void deletar(String numeroCarga) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada para o número: " + numeroCarga));

        cargaRepository.delete(carga);
    }
}
