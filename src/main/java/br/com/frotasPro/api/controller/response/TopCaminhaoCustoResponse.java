package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopCaminhaoCustoResponse {
    private String codigoCaminhao;
    private String descricaoCaminhao;
    private BigDecimal total;
}
