package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarTodasMetasService {

    private final MetaRepository metaRepository;

    public Page<MetaResponse> listar(Pageable pageable) {
        Page<Meta> page = metaRepository.findAll(pageable);
        return page.map(MetaMapper::toResponse);
    }
}
