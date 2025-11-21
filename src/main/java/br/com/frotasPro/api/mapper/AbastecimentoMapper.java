package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;

public class AbastecimentoMapper {

    public static AbastecimentoResponse toResponse(Abastecimento a) {
        return AbastecimentoResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())
                .paradaId(a.getParadaCarga().getId())
                .caminhaoId(a.getCaminhao().getId())
                .motoristaId(a.getMotorista().getId() )
                .dtAbastecimento(a.getDtAbastecimento())
                .kmOdometro(a.getKmOdometro())
                .qtLitros(a.getQtLitros())
                .valorLitro(a.getValorLitro())
                .valorTotal(a.getValorTotal())
                .tipoCombustivel(a.getTipoCombustivel().name())
                .formaPagamento(a.getFormaPagamento().name())
                .posto(a.getPosto())
                .cidade(a.getCidade())
                .uf(a.getUf())
                .numNotaOuCupom(a.getNumNotaOuCupom())
                .build();
    }
}
