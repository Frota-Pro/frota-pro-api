package br.com.frotasPro.api.modules.meta.mapper;

import br.com.frotasPro.api.modules.meta.dto.response.MetaResponse;
import br.com.frotasPro.api.modules.meta.domain.Meta;

import java.math.BigDecimal;

public class MetaMapper {

    public static MetaResponse toResponse(Meta meta) {
        return toResponse(meta, meta != null ? meta.getValorRealizado() : null, null, null);
    }

    public static MetaResponse toResponse(Meta meta, BigDecimal valorRealizado, BigDecimal percentual, Boolean metaAtingida) {
        return toResponse(meta, valorRealizado, percentual, metaAtingida, null);
    }

    public static MetaResponse toResponse(Meta meta, BigDecimal valorRealizado, BigDecimal percentual, Boolean metaAtingida, String statusDesempenho) {
        if (meta == null) {
            return null;
        }

        MetaResponse.MetaResponseBuilder builder = MetaResponse.builder()
                .id(meta.getId())
                .dataIncio(meta.getDataIncio())
                .dataFim(meta.getDataFim())
                .tipoMeta(meta.getTipoMeta())
                .regraAtingimento(meta.getTipoMeta() != null ? meta.getTipoMeta().getRegraAtingimento() : null)
                .valorMeta(meta.getValorMeta())
                .valorRealizado(valorRealizado)
                .percentual(percentual)
                .metaAtingida(metaAtingida)
                .statusDesempenho(statusDesempenho)
                .unidade(meta.getUnidade())
                .statusMeta(meta.getStatusMeta())
                .descricao(meta.getDescricao())
                .renovarAutomaticamente(meta.isRenovarAutomaticamente());


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
