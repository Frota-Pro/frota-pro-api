package br.com.frotasPro.api.service.conta;

import br.com.frotasPro.api.controller.response.ContaResponse;
import br.com.frotasPro.api.mapper.ContaMapper;
import br.com.frotasPro.api.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarContaService {

    private final ContaRepository repository;

    public List<ContaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(ContaMapper::toResponse)
                .toList();
    }
}
