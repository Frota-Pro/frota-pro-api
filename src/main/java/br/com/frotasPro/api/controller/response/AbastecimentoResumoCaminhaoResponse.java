package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AbastecimentoResumoCaminhaoResponse {

    private String caminhao;
    private BigDecimal totalLitros;
    private BigDecimal totalValor;
}
