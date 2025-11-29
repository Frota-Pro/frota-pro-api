package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.domain.DespesaParada;
import org.springframework.stereotype.Component;

@Component
public class DespesaParadaMapper {

    public static DespesaParadaResponse toResponse(DespesaParada entity) {
        if (entity == null) return null;

        DespesaParadaResponse r = new DespesaParadaResponse();
        r.setId(entity.getId());
        r.setTipoDespesa(entity.getTipoDespesa().getDescricao());
        r.setDataHora(entity.getDataHora());
        r.setValor(entity.getValor());
        r.setDescricao(entity.getDescricao());
        r.setCidade(entity.getCidade());
        r.setUf(entity.getUf());

        return r;
    }
}
