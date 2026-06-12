package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.DespesaParadaResponse;
import br.com.frotasPro.api.modules.logistica.mapper.DespesaParadaMapper;
import br.com.frotasPro.api.modules.logistica.repository.DespesaParadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodosDespesaParadaService {

    private final DespesaParadaRepository repository;
    public List<DespesaParadaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(DespesaParadaMapper::toResponse)
                .toList();
    }
}
