package br.com.frotasPro.api.service.despesaParada;

import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.mapper.DespesaParadaMapper;
import br.com.frotasPro.api.repository.DespesaParadaRepository;
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
