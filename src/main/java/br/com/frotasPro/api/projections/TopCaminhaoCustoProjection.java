package br.com.frotasPro.api.projections;

import java.math.BigDecimal;

public interface TopCaminhaoCustoProjection {
    String getCodigoCaminhao();
    String getDescricaoCaminhao();
    BigDecimal getTotal();
}
