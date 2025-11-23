package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AbastecimentoParadaRequest {

    @NotNull(message = "Quantidade de litros é obrigatória")
    private BigDecimal qtLitros;

    @NotNull(message = "Valor do litro é obrigatório")
    private BigDecimal valorLitro;

    private BigDecimal valorTotal;

    @NotNull(message = "Tipo de combustível é obrigatório")
    private TipoCombustivel tipoCombustivel;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    private String posto;
    private String cidade;
    private String uf;
    private String numNotaOuCupom;
}

