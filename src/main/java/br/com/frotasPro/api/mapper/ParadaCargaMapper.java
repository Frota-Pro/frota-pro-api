package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.controller.response.ManutencaoResponse;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.DespesaParada;
import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.ParadaCarga;

import java.util.List;
import java.util.stream.Collectors;

public class ParadaCargaMapper {

    public static ParadaCarga toEntity(ParadaCargaRequest request, Carga carga) {
        return ParadaCarga.builder()
                .carga(carga)
                .tipoParada(request.getTipoParada())
                .dtInicio(request.getDtInicio())
                .dtFim(request.getDtFim())
                .cidade(request.getCidade())
                .local(request.getLocal())
                .kmOdometro(request.getKmOdometro())
                .observacao(request.getObservacao())
                .build();
    }

    public static ParadaCargaResponse toResponse(ParadaCarga entity) {
        ParadaCargaResponse r = new ParadaCargaResponse();
        r.setId(entity.getId());

        String codigo = entity.getCarga().getNumeroCargaExterno() != null
                ? entity.getCarga().getNumeroCargaExterno()
                : entity.getCarga().getNumeroCarga();

        r.setCodigocarga(codigo);
        r.setTipoParada(entity.getTipoParada().getDescricao());
        r.setDtInicio(entity.getDtInicio());
        r.setDtFim(entity.getDtFim());
        r.setCidade(entity.getCidade());
        r.setLocal(entity.getLocal());
        r.setKmOdometro(entity.getKmOdometro());
        r.setObservacao(entity.getObservacao());

        if (entity.getDespesaParadas() != null && !entity.getDespesaParadas().isEmpty()) {
            List<DespesaParadaResponse> despesas = entity.getDespesaParadas()
                    .stream()
                    .map(ParadaCargaMapper::toDespesaParadaResponse)
                    .collect(Collectors.toList());

            r.setDespesaParadas(despesas);
        }

        if (entity.getManutencoes() != null && !entity.getManutencoes().isEmpty()) {
            Manutencao primeira = entity.getManutencoes().get(0);
            r.setManutencao(toManutencaoResponse(primeira));
        }

        return r;
    }

    private static DespesaParadaResponse toDespesaParadaResponse(DespesaParada d) {
        DespesaParadaResponse r = new DespesaParadaResponse();
        r.setId(d.getId());
        r.setTipoDespesa(d.getTipoDespesa().getDescricao());
        r.setDataHora(d.getDataHora());
        r.setValor(d.getValor());
        r.setDescricao(d.getDescricao());
        r.setCidade(d.getCidade());
        r.setUf(d.getUf());
        return r;
    }

    private static ManutencaoResponse toManutencaoResponse(Manutencao m) {
        ManutencaoResponse r = new ManutencaoResponse();
        r.setId(m.getId());
        r.setCodigo(m.getCodigo());
        r.setDescricao(m.getDescricao());
        r.setDataInicioManutencao(m.getDataInicioManutencao());
        r.setDataFimManutencao(m.getDataFimManutencao());

        r.setTipoManutencao(m.getTipoManutencao());
        r.setItensTrocados(m.getItensTrocados());
        r.setObservacoes(m.getObservacoes());
        r.setValor(m.getValor());
        r.setStatusManutencao(m.getStatusManutencao());

        return r;
    }
}
