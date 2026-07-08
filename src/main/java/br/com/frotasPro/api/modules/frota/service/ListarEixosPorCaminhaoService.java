package br.com.frotasPro.api.modules.frota.service;

import br.com.frotasPro.api.modules.frota.dto.response.EixoResponse;
import br.com.frotasPro.api.modules.frota.mapper.EixoMapper;
import br.com.frotasPro.api.modules.frota.repository.EixoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarEixosPorCaminhaoService {

    private final EixoRepository eixoRepository;

    public Page<EixoResponse> listarPorCaminhao(String codigoCaminhao, Pageable pageable) {
        return eixoRepository.findByCaminhaoCodigo(codigoCaminhao, pageable)
                .map(EixoMapper::toResponse);
    }
}
