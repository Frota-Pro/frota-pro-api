package br.com.frotasPro.api.service.pneu;


import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.domain.Pneu;
import br.com.frotasPro.api.repository.EixoRepository;
import br.com.frotasPro.api.repository.PneuRepository;
import br.com.frotasPro.api.validator.ValidaSePneuExiste;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static br.com.frotasPro.api.mapper.PneuMapper.toResponse;

@Service
@RequiredArgsConstructor
public class AtualizarPneuService {

    private final PneuRepository pneuRepository;
    private final EixoRepository eixoRepository;
    private final ValidaSePneuExiste validaSePneuExiste;

    public PneuResponse atualizar(UUID id, PneuRequest request) {

        validaSePneuExiste.validar(id);

        Pneu pneu = pneuRepository.findById(id).get();

        pneu.setPosicao(request.getPosicao());
        pneu.setUltimaTroca(request.getUltimaTroca());

        Eixo novoEixo = eixoRepository.findById(request.getEixoId())
                .orElseThrow(() -> new RuntimeException("Eixo não encontrado"));

        pneu.setEixo(novoEixo);

        pneuRepository.save(pneu);

        return toResponse(pneu);
    }
}

