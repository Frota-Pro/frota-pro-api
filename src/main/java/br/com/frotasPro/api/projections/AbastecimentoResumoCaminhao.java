package br.com.frotasPro.api.projections;

import java.math.BigDecimal;

public interface AbastecimentoResumoCaminhao {
    String getCaminhao();
    BigDecimal getTotalLitros();
    BigDecimal getTotalValor();
}
