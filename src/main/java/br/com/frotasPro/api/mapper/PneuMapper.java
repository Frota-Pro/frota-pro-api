package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.PneuRequest;
import br.com.frotasPro.api.controller.response.PneuResponse;
import br.com.frotasPro.api.domain.Eixo;
import br.com.frotasPro.api.domain.Pneu;

public class PneuMapper {

    public static Pneu toEntity(PneuRequest request, Eixo eixo) {
        return Pneu.builder()
                .posicao(request.getPosicao())
                .ultimaTroca(request.getUltimaTroca())
                .eixo(eixo)
                .build();
    }

    public static PneuResponse toResponse(Pneu pneu) {
        return PneuResponse.builder()
                .id(pneu.getId())
                .posicao(pneu.getPosicao())
                .ultimaTroca(pneu.getUltimaTroca())
                .eixoId(pneu.getEixo().getId())
                .build();
    }
}
