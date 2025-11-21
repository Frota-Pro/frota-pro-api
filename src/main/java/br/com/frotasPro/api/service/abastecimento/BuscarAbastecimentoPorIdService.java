package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarAbastecimentoPorIdService {

    private final AbastecimentoRepository repository;

    public AbastecimentoResponse buscar(UUID id) {
        return repository.findById(id)
                .map(AbastecimentoMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Abastecimento não encontrado"));
    }
}

