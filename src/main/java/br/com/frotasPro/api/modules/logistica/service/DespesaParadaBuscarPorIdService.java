package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.DespesaParadaResponse;
import br.com.frotasPro.api.modules.logistica.domain.DespesaParada;
import br.com.frotasPro.api.modules.logistica.repository.DespesaParadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.modules.logistica.mapper.DespesaParadaMapper.toResponse;

@Service
@RequiredArgsConstructor
public class DespesaParadaBuscarPorIdService {

    private final DespesaParadaRepository repository;

    public DespesaParadaResponse buscar(UUID id) {

        DespesaParada entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        return toResponse(entity);
    }
}
