package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.DespesaParadaResponse;
import br.com.frotasPro.api.domain.DespesaParada;
import org.springframework.stereotype.Component;

@Component
public class DespesaParadaMapper {

    public static DespesaParadaResponse toResponse(DespesaParada d) {
        return DespesaParadaResponse.builder()
                .id(d.getId())
                .paradaId(d.getParadaCarga().getId())
                .tipoDespesa(d.getTipoDespesa().name())
                .dataHora(d.getDataHora())
                .valor(d.getValor())
                .descricao(d.getDescricao())
                .cidade(d.getCidade())
                .uf(d.getUf())
                .build();
    }
}
