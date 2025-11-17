package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuscarCargaService {

    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public CargaResponse buscar(String numeroCarga) {
        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada para o número: " + numeroCarga));

        return CargaMapper.toResponse(carga);
    }
}
