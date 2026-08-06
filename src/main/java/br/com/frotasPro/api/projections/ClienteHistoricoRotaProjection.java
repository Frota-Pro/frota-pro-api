package br.com.frotasPro.api.projections;

import java.time.LocalDate;

public interface ClienteHistoricoRotaProjection {
    String getCliente();
    String getCidade();
    Long getQuantidadeCargas();
    LocalDate getUltimaCargaEm();
}
