package br.com.frotasPro.api.service.eixo;

import br.com.frotasPro.api.controller.response.EixoResponse;
import br.com.frotasPro.api.mapper.EixoMapper;
import br.com.frotasPro.api.repository.EixoRepository;
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
