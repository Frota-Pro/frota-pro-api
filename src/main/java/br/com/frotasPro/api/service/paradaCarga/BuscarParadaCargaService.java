package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarParadaCargaService {

    private final ParadaCargaRepository paradaCargaRepository;

    @Transactional(readOnly = true)
    public ParadaCargaResponse buscar(UUID id) {
        ParadaCarga parada = paradaCargaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Parada não encontrada: " + id));
        return ParadaCargaMapper.toResponse(parada);
    }
}