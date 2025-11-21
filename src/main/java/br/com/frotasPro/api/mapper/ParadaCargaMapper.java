package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.request.ParadaCargaRequest;
import br.com.frotasPro.api.controller.response.ParadaCargaResponse;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoParada;

public class ParadaCargaMapper {

    public static ParadaCarga toEntity(ParadaCargaRequest request, Carga carga) {
        return ParadaCarga.builder()
                .carga(carga)
                .tipoParada(TipoParada.valueOf(request.getTipoParada()))
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
        r.setCargaId(entity.getCarga().getId());
        r.setTipoParada(entity.getTipoParada().name());
        r.setDtInicio(entity.getDtInicio());
        r.setDtFim(entity.getDtFim());
        r.setCidade(entity.getCidade());
        r.setLocal(entity.getLocal());
        r.setKmOdometro(entity.getKmOdometro());
        r.setObservacao(entity.getObservacao());
        return r;
    }
}

