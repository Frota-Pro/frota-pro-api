package br.com.frotasPro.api.modules.logistica.mapper;

import br.com.frotasPro.api.modules.logistica.dto.response.DespesaParadaResponse;
import br.com.frotasPro.api.modules.logistica.domain.DespesaParada;
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
