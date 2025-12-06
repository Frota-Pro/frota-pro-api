package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoMeta {
    QUILOMETRAGEM("Meta de quilometragem"),
    CONSUMO_COMBUSTIVEL("Meta de consumo de combustivel"),
    TONELADA("Meta de tonelada da carga"),
    CARGA_TRANSPORTADA("Meta de carga trasportada");

    private final String descricao;
}
