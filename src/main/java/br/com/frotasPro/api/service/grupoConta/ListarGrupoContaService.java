package br.com.frotasPro.api.service.grupoConta;

import br.com.frotasPro.api.controller.response.GrupoContaResponse;
import br.com.frotasPro.api.mapper.GrupoContaMapper;
import br.com.frotasPro.api.repository.GrupoContaRepository;
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
