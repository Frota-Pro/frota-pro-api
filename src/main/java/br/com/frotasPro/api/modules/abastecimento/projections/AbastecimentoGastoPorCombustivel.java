package br.com.frotasPro.api.modules.abastecimento.projections;

import java.math.BigDecimal;

import br.com.frotasPro.api.shared.enums.TipoCombustivel;

public interface AbastecimentoGastoPorCombustivel {

    TipoCombustivel getTipoCombustivel();

    BigDecimal getTotalLitros();

    BigDecimal getTotalValor();
}
