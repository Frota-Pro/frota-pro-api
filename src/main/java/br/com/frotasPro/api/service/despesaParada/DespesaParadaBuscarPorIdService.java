package br.com.frotasPro.api.service.despesaParada;

import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.repository.DespesaParadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.DespesaParadaMapper.toResponse;

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
