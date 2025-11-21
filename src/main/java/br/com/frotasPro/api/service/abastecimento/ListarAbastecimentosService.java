package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.mapper.AbastecimentoMapper;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarAbastecimentosService {

    private final AbastecimentoRepository repository;

    public List<AbastecimentoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(AbastecimentoMapper::toResponse)
                .toList();
    }
}

