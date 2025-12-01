package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.mapper.MetaMapper;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.util.ProgressoMetaKmLitroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarMetaAtivaComProgressoService {

    private final MetaRepository metaRepository;
    private final ProgressoMetaKmLitroService progressoMetaKmLitroService;

    public MetaResponse buscarMetaAtivaCaminhao(String caminhaoCodigo, LocalDate dataReferencia) {

        List<Meta> metas = metaRepository.buscarMetasAtivasPorAlvoEData(
                null,
                StatusMeta.EM_ANDAMENTO,
                dataReferencia,
                caminhaoCodigo,
                null
        );

        if (metas.isEmpty()) return null;

        Meta meta = metas.get(0);
        MetaResponse base = MetaMapper.toResponse(meta);

        if (meta.getTipoMeta() == TipoMeta.CONSUMO_COMBUSTIVEL) {
            BigDecimal realizado = progressoMetaKmLitroService.calcularProgressoKmLitroParaMeta(meta);
            base = MetaResponse.builder()
                    .id(base.getId())
                    .dataIncio(base.getDataIncio())
                    .dataFim(base.getDataFim())
                    .tipoMeta(base.getTipoMeta())
                    .valorMeta(base.getValorMeta())
                    .valorRealizado(realizado)
                    .unidade(base.getUnidade())
                    .statusMeta(base.getStatusMeta())
                    .descricao(base.getDescricao())
                    .caminhaoCodigo(base.getCaminhaoCodigo())
                    .caminhaoDescricao(base.getCaminhaoDescricao())
                    .categoriaCodigo(base.getCategoriaCodigo())
                    .categoriaDescricao(base.getCategoriaDescricao())
                    .motoristaCodigo(base.getMotoristaCodigo())
                    .motoristaDescricao(base.getMotoristaDescricao())
                    .renovarAutomaticamente(base.isRenovarAutomaticamente())
                    .build();
        }

        return base;
    }
}

