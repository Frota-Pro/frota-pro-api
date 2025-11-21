package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarParadaCargaService {

    private final ParadaCargaRepository repository;

    public List<ParadaCargaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(ParadaCargaMapper::toResponse)
                .toList();
    }
}
