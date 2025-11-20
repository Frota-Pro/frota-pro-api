package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.frotasPro.api.mapper.PneuMapper.toEntity;
import static br.com.frotasPro.api.mapper.PneuMapper.toResponse;


@Service
@RequiredArgsConstructor
public class CriarPneuService {

    private final PneuRepository pneuRepository;
    private final EixoRepository eixoRepository;

    public PneuResponse criar(PneuRequest request) {

        Eixo eixo = eixoRepository.findById(request.getEixoId())
                .orElseThrow(() -> new RuntimeException("Eixo não encontrado"));

        Pneu pneu =toEntity(request, eixo);

        pneuRepository.save(pneu);

        return toResponse(pneu);
    }

}