package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaMinResponse;
import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarCargaService {

    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public Page<CargaMinResponse> listar(Pageable pageable) {
        Page<Carga> cargas = cargaRepository.findAll(pageable);
        return cargas.map(CargaMapper::toMinResponse);
    }
}
