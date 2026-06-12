package br.com.frotasPro.api.modules.financeiro.service;

import br.com.frotasPro.api.modules.financeiro.dto.response.GrupoContaResponse;
import br.com.frotasPro.api.modules.financeiro.mapper.GrupoContaMapper;
import br.com.frotasPro.api.modules.financeiro.repository.GrupoContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarGrupoContaService {

    private final GrupoContaRepository repository;

    public List<GrupoContaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(GrupoContaMapper::toResponse)
                .toList();
    }
}
