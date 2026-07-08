package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.RotaResponse;
import br.com.frotasPro.api.modules.logistica.domain.Rota;
import br.com.frotasPro.api.modules.logistica.mapper.RotaMapper;
import br.com.frotasPro.api.modules.logistica.repository.RotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarRotaService {

    private final RotaRepository repository;

    @Transactional(readOnly = true)
    public Page<RotaResponse> listar(Pageable pageable) {
        Page<Rota> page = repository.findAll(pageable);
        return page.map(RotaMapper::toResponse);
    }
}
