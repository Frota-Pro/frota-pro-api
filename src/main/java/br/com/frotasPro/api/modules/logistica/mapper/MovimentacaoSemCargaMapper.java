package br.com.frotasPro.api.modules.logistica.mapper;

import br.com.frotasPro.api.modules.logistica.dto.response.MovimentacaoSemCargaResponse;
import br.com.frotasPro.api.modules.logistica.domain.MovimentacaoSemCarga;

public class MovimentacaoSemCargaMapper {

    public static MovimentacaoSemCargaResponse toResponse(MovimentacaoSemCarga movimentacao) {
        if (movimentacao == null) {
            return null;
        }

        return MovimentacaoSemCargaResponse.builder()
                .id(movimentacao.getId())
                .dataMovimentacao(movimentacao.getDataMovimentacao())
                .codigoCaminhao(movimentacao.getCaminhao() != null ? movimentacao.getCaminhao().getCodigo() : null)
                .placaCaminhao(movimentacao.getCaminhao() != null ? movimentacao.getCaminhao().getPlaca() : null)
                .numeroCargaInicio(movimentacao.getCargaInicio() != null ? movimentacao.getCargaInicio().getNumeroCarga() : null)
                .kmOrigem(movimentacao.getKmOrigem())
                .kmDestino(movimentacao.getKmDestino())
                .kmRodado(movimentacao.getKmRodado())
                .mediaKmLitroUsada(movimentacao.getMediaKmLitroUsada())
                .valorLitroMedio(movimentacao.getValorLitroMedio())
                .custoEstimado(movimentacao.getCustoEstimado())
                .build();
    }
}
