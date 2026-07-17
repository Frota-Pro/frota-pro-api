package br.com.frotasPro.api.projections;

import java.time.LocalDate;

public interface ClienteHistoricoRotaProjection {
    String getCliente();
    Long getQuantidadeCargas();
    LocalDate getUltimaCargaEm();
}
