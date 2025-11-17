package br.com.frotasPro.api.service.rota;

import br.com.frotasPro.api.controller.response.RotaResponse;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.mapper.RotaMapper;
import br.com.frotasPro.api.repository.RotaRepository;
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
