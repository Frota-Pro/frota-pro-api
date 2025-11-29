package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;

public class MetaMapper {

    public static MetaResponse toResponse(Meta meta) {
        if (meta == null) {
            return null;
        }

        MetaResponse.MetaResponseBuilder builder = MetaResponse.builder()
                .id(meta.getId())
                .dataIncio(meta.getDataIncio())
                .dataFim(meta.getDataFim())
                .tipoMeta(meta.getTipoMeta())
                .valorMeta(meta.getValorMeta())
                .valorRealizado(meta.getValorRealizado())
                .unidade(meta.getUnidade())
                .statusMeta(meta.getStatusMeta())
                .descricao(meta.getDescricao());

        if (meta.getCaminhao() != null) {
            builder
                    .caminhaoCodigo(meta.getCaminhao().getCodigo())
                    .caminhaoDescricao(meta.getCaminhao().getDescricao());
        }

        if (meta.getCategoria() != null) {
            builder
                    .categoriaCodigo(meta.getCategoria().getCodigo())
                    .categoriaDescricao(meta.getCategoria().getDescricao());
        }

        if (meta.getMotorista() != null) {
            builder
                    .motoristaCodigo(meta.getMotorista().getCodigo())
                    .motoristaDescricao(meta.getMotorista().getNome());
        }

        return builder.build();
    }
}
