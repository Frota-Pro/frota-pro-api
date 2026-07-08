package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.domain.Carga;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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
