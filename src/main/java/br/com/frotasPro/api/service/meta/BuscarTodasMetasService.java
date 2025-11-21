package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodasMetasService {

    private final MetaRepository metaRepository;

    public List<MetaResponse> listar() {
        return metaRepository.findAll().stream()
                .map(MetaMapper::toResponse)
                .toList();
    }
}

