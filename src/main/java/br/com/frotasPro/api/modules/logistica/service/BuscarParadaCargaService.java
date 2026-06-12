package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.ParadaCargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
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