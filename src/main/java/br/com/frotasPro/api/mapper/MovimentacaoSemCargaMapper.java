package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MovimentacaoSemCargaResponse;
import br.com.frotasPro.api.domain.MovimentacaoSemCarga;

public class MovimentacaoSemCargaMapper {

    public static MovimentacaoSemCargaResponse toResponse(MovimentacaoSemCarga movimentacao, boolean integracaoAtiva) {
        if (movimentacao == null) {
            return null;
        }

        String numeroCargaInicioExibicao = movimentacao.getCargaInicio() != null
                ? CargaMapper.resolverNumeroExibicao(
                        movimentacao.getCargaInicio().getNumeroCarga(),
                        movimentacao.getCargaInicio().getNumeroCargaExterno(),
                        integracaoAtiva)
                : null;

        return MovimentacaoSemCargaResponse.builder()
                .id(movimentacao.getId())
                .dataMovimentacao(movimentacao.getDataMovimentacao())
                .codigoCaminhao(movimentacao.getCaminhao() != null ? movimentacao.getCaminhao().getCodigo() : null)
                .placaCaminhao(movimentacao.getCaminhao() != null ? movimentacao.getCaminhao().getPlaca() : null)
                .numeroCargaInicio(movimentacao.getCargaInicio() != null ? movimentacao.getCargaInicio().getNumeroCarga() : null)
                .numeroCargaInicioExibicao(numeroCargaInicioExibicao)
                .kmOrigem(movimentacao.getKmOrigem())
                .kmDestino(movimentacao.getKmDestino())
                .kmRodado(movimentacao.getKmRodado())
                .mediaKmLitroUsada(movimentacao.getMediaKmLitroUsada())
                .valorLitroMedio(movimentacao.getValorLitroMedio())
                .custoEstimado(movimentacao.getCustoEstimado())
                .build();
    }
}
