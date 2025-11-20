package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;

public class ManutencaoMapper {

    public static ManutencaoResponse toResponse(Manutencao manutencao) {

        return ManutencaoResponse.builder()
                .id(manutencao.getId())
                .codigo(manutencao.getCodigo())
                .descricao(manutencao.getDescricao())
                .dataInicioManutencao(manutencao.getDataInicioManutencao())
                .dataFimManutencao(manutencao.getDataFimManutencao())
                .tipoManutencao(manutencao.getTipoManutencao())
                .itensTrocados(manutencao.getItensTrocados())
                .observacoes(manutencao.getObservacoes())
                .valor(manutencao.getValor())
                .statusManutencao(manutencao.getStatusManutencao())
                .caminhaoId(manutencao.getCaminhao().getId())
                .oficinaId(
                        manutencao.getOficina() != null
                                ? manutencao.getOficina().getId()
                                : null
                )
                .build();
    }
}

