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
public class AtualizarPneuService {

    private final PneuRepository pneuRepository;
    private final EixoRepository eixoRepository;

    @Transactional
    public PneuResponse atualizar(String codigo, PneuRequest request) {

        Pneu pneu = pneuRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ObjectNotFound("Pneu não encontrado para o código: " + codigo));

        if (request.getEixoId() != null &&
                !request.getEixoId().equals(pneu.getEixo().getId())) {

            Eixo eixo = eixoRepository.findById(request.getEixoId())
                    .orElseThrow(() -> new ObjectNotFound("Eixo não encontrado para o id: " + request.getEixoId()));

            pneu.setEixo(eixo);
        }

        // Atualiza dados opcionais
        if (request.getPosicao() != null) {
            pneu.setPosicao(request.getPosicao());
        }

        if (request.getLadoAtual() != null) {
            pneu.setLadoAtual(request.getLadoAtual());
        }

        if (request.getPosicaoAtual() != null) {
            pneu.setPosicaoAtual(request.getPosicaoAtual());
        }

        pneuRepository.save(pneu);

        return PneuMapper.toResponse(pneu);
    }
}
