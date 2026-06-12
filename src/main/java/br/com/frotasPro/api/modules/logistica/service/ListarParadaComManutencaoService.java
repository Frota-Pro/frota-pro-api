package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.logistica.dto.response.ParadaCargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.ParadaCarga;
import br.com.frotasPro.api.modules.logistica.mapper.ParadaCargaMapper;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.repository.ParadaCargaRepository;
import br.com.frotasPro.api.shared.enums.TipoParada;
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
