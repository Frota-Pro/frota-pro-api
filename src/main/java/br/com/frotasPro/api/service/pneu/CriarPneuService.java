package br.com.frotasPro.api.service.pneu;

import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.mapper.PneuMapper;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.repository.PneuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriarPneuService {

    private final PneuRepository pneuRepository;
    private final EixoRepository eixoRepository;

    @Transactional
    public PneuResponse criar(PneuRequest request) {

        Eixo eixo = eixoRepository.findById(request.getEixoId())
                .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + request.getEixoId()));

        Pneu pneu = PneuMapper.toEntity(request, eixo);

        pneuRepository.save(pneu);

        return PneuMapper.toResponse(pneu);
    }
}
