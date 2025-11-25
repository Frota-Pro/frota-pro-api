package br.com.frotasPro.api.projections;

import br.com.frotasPro.api.domain.enums.TipoCombustivel;

import java.math.BigDecimal;

public interface AbastecimentoGastoPorCombustivel {

    TipoCombustivel getTipoCombustivel();

    BigDecimal getTotalLitros();

    BigDecimal getTotalValor();
}
