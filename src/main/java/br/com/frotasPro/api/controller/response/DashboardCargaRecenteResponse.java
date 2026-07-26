package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardCargaRecenteResponse {

    private String numeroCarga;
    /** Número a ser exibido ao usuário: externo se a integração estiver ativa, senão interno. */
    private String numeroCargaExibicao;
    private String origem;
    private String destino;

    private BigDecimal valorTotal;
    private BigDecimal pesoCarga;

    private String status;     // Status.name()
    private LocalDate dtSaida;
}
