package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Uma linha da lista de planilhas. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanejamentoPlanilhaResumoResponse {

    private UUID id;
    private LocalDate data;
    private String status;
    private int totalCarregamentos;
    private int totalRotas;
    private BigDecimal pesoToneladas;
}
