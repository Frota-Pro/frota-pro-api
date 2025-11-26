package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.domain.Manutencao;

public class ManutencaoMapper {

    private ManutencaoMapper() {
    }

    public static ManutencaoResponse toResponse(Manutencao entity) {
        if (entity == null) {
            return null;
        }

        String caminhaoCodigo = null;
        String caminhaoDescricao = null;
        if (entity.getCaminhao() != null) {
            caminhaoCodigo = entity.getCaminhao().getCodigo();
            caminhaoDescricao = entity.getCaminhao().getDescricao();
        }

        String oficinaCodigo = null;
        String oficinaNome = null;
        if (entity.getOficina() != null) {
            oficinaCodigo = entity.getOficina().getCodigo();
            oficinaNome = entity.getOficina().getNome();
        }

        return ManutencaoResponse.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .dataInicioManutencao(entity.getDataInicioManutencao())
                .dataFimManutencao(entity.getDataFimManutencao())
                .tipoManutencao(entity.getTipoManutencao())
                .itensTrocados(entity.getItensTrocados())
                .observacoes(entity.getObservacoes())
                .valor(entity.getValor())
                .statusManutencao(entity.getStatusManutencao())

                .codigoCaminhao(caminhaoCodigo)
                .caminhao(caminhaoDescricao)
                .codigoOficina(oficinaCodigo)
                .oficina(oficinaNome)
                .build();
    }
}
