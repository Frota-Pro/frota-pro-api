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

        // Vale para todos os tipos, inclusive consumo: a meta de consumo e em km/L, entao quanto
        // MAIS o caminhao rende, melhor ("pelo menos 2,5 km por litro").
        return realizado.compareTo(valorMeta) >= 0;
    }

    public String getRegraAtingimento() {
        return "MAIOR_OU_IGUAL";
    }
}
