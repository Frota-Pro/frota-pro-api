package br.com.frotasPro.api.modules.abastecimento.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import br.com.frotasPro.api.shared.enums.TipoCombustivel;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AbastecimentoGastoPorCombustivelResponse {

    private TipoCombustivel tipoCombustivel;
    private BigDecimal totalLitros;
    private BigDecimal totalValor;
}
