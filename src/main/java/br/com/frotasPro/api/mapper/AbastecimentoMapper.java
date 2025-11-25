package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.AbastecimentoResponse;
import br.com.frotasPro.api.domain.Abastecimento;

public class AbastecimentoMapper {

    public static AbastecimentoResponse toResponse(Abastecimento a) {
        return AbastecimentoResponse.builder()
                .id(a.getId())
                .codigo(a.getCodigo())

                .paradaId(
                        a.getParadaCarga() != null
                                ? a.getParadaCarga().getId()
                                : null
                )

                .caminhaoCodigo(
                        a.getCaminhao() != null
                                ? a.getCaminhao().getCodigo()
                                : null
                )
                .caminhaoPlaca(
                        a.getCaminhao() != null
                                ? a.getCaminhao().getPlaca()
                                : null
                )

                .motoristaCodigo(
                        a.getMotorista() != null
                                ? a.getMotorista().getCodigo()
                                : null
                )

                .dtAbastecimento(a.getDtAbastecimento())
                .kmOdometro(a.getKmOdometro())
                .qtLitros(a.getQtLitros())
                .valorLitro(a.getValorLitro())
                .valorTotal(a.getValorTotal())
                .mediaKmLitro(a.getMediaKmLitro())

                .tipoCombustivel(a.getTipoCombustivel() != null
                        ? a.getTipoCombustivel().name()
                        : null)
                .formaPagamento(a.getFormaPagamento() != null
                        ? a.getFormaPagamento().name()
                        : null)

                .posto(a.getPosto())
                .cidade(a.getCidade())
                .uf(a.getUf())
                .numNotaOuCupom(a.getNumNotaOuCupom())
                .build();
    }
}
