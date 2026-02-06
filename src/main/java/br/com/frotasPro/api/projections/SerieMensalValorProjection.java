package br.com.frotasPro.api.projections;

import java.math.BigDecimal;

public interface SerieMensalValorProjection {
    Integer getAno();
    Integer getMes();
    BigDecimal getTotal();
}
