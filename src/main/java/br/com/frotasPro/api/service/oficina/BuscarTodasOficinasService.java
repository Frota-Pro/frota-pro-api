package br.com.frotasPro.api.service.oficina;

import br.com.frotasPro.api.controller.response.OficinaResponse;
import br.com.frotasPro.api.domain.Oficina;
import br.com.frotasPro.api.mapper.OficinaMapper;
import br.com.frotasPro.api.repository.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarTodasOficinasService {

    private final OficinaRepository oficinaRepository;

    @Transactional(readOnly = true)
    public Page<OficinaResponse> listar(Pageable pageable) {
        Page<Oficina> oficinas = oficinaRepository.findAll(pageable);
        return oficinas.map(OficinaMapper::toResponse);
    }
}
