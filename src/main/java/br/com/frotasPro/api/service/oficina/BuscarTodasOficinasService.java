package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.mapper.OficinaMapper;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodasOficinasService {

    private final OficinaRepository repository;

    public List<OficinaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(OficinaMapper::toResponse)
                .toList();
    }
}
