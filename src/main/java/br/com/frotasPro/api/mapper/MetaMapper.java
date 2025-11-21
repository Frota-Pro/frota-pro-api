package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;

public class MetaMapper {

    public static MetaResponse toResponse(Meta meta) {
        return MetaResponse.builder()
                .id(meta.getId())
                .dataIncio(meta.getDataIncio())
                .dataFim(meta.getDataFim())
                .tipoMeta(meta.getTipoMeta())
                .valorMeta(meta.getValorMeta())
                .valorRealizado(meta.getValorRealizado())
                .unidade(meta.getUnidade())
                .statusMeta(meta.getStatusMeta())
                .descricao(meta.getDescricao())
                .caminhaoId(meta.getCaminhao().getId())
                .motoristaId(meta.getMotorista().getId())
                .build();
    }
}
