package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.response.ContaResponse;
import br.com.frotasPro.api.modules.financeiro.mapper.ContaMapper;
import br.com.frotasPro.api.modules.financeiro.repository.ContaRepository;
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
