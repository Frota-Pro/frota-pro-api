package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.ParadaCargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.exception.ObjectNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarParadaCargaService {

    private final ParadaCargaRepository paradaRepository;
    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public Page<ParadaCargaResponse> listarPorCarga(String numeroCarga, Pageable pageable) {

        cargaRepository.findByNumeroCarga(numeroCarga)
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada"));

        Page<ParadaCarga> page = paradaRepository.findByCargaNumeroCarga(numeroCarga, pageable);
        return page.map(ParadaCargaMapper::toResponse);
    }
}
