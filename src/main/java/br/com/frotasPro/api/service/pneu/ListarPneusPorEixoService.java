package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.mapper.PneuMapper;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListarPneusPorEixoService {

    private final PneuRepository pneuRepository;

    public Page<PneuResponse> listarPorEixo(UUID eixoId, Pageable pageable) {
        return pneuRepository.findByEixoId(eixoId, pageable)
                .map(PneuMapper::toResponse);
    }
}
