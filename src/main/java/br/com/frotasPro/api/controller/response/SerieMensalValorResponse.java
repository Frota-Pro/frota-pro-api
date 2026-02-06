package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SerieMensalValorResponse {
    // ex: "2026-02"
    private String mes;
    private BigDecimal total;
}
