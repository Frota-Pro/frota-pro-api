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

    public boolean metaAtingida(java.math.BigDecimal realizado, java.math.BigDecimal valorMeta) {
        if (realizado == null || valorMeta == null || valorMeta.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (this == CONSUMO_COMBUSTIVEL) {
            return realizado.compareTo(valorMeta) <= 0;
        }

        return realizado.compareTo(valorMeta) >= 0;
    }

    public String getRegraAtingimento() {
        if (this == CONSUMO_COMBUSTIVEL) {
            return "MENOR_OU_IGUAL";
        }
        return "MAIOR_OU_IGUAL";
    }
}
