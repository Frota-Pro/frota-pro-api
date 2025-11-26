package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.mapper.PneuMapper;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarPneusPorCaminhaoService {

    private final PneuRepository pneuRepository;

    public Page<PneuResponse> listarPorCaminhao(String codigoCaminhao, Pageable pageable) {
        return pneuRepository.findByEixoCaminhaoCodigo(codigoCaminhao, pageable)
                .map(PneuMapper::toResponse);
    }
}
