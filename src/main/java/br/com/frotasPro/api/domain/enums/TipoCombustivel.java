package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoCombustivel {
    DIESEL_S10("Diesel S-10"),
    DIESEL_S500("Diesel S-500"),
    DIESEL_COMUM("Diesel Comum"),
    GASOLINA_COMUM("Gasolina Comum"),
    GASOLINA_ADITIVADA("Gasolina Aditivada"),
    GASOLINA_PREMIUM("Gasolina Premium"),
    ETANOL("Etanol"),
    ETANOL_ADITIVADO("Etanol Aditivado"),
    GNV("Gás Natural Veicular"),
    ARLA32("ARLA 32"),
    ELETRICO("Elétrico"),
    HIBRIDO("Híbrido");

    private final String descricao;

}
