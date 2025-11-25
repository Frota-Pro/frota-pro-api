package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AbastecimentoGastoPorCombustivelResponse {

    private TipoCombustivel tipoCombustivel;
    private BigDecimal totalLitros;
    private BigDecimal totalValor;
}
