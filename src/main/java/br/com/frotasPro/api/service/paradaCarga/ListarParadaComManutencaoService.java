package br.com.frotasPro.api.service.paradaCarga;

import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoParada;
import br.com.frotasPro.api.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ParadaCargaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarParadaComManutencaoService {

    private final ParadaCargaRepository paradaRepository;
    private final CargaRepository cargaRepository;

    @Transactional(readOnly = true)
    public Page<ParadaCargaResponse> listarParadasComManutencao(
            String numeroCarga,
            Pageable pageable
    ) {
        Page<ParadaCarga> page = paradaRepository
                .findParadasComManutencaoPorCarga(numeroCarga, pageable);

        return page.map(ParadaCargaMapper::toResponse);
    }
}
