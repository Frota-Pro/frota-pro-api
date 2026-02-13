package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AbastecimentoParadaRequest {

    @NotNull(message = "Quantidade de litros é obrigatória")
    @DecimalMin(value = "0.0001", message = "Quantidade de litros deve ser maior que zero")
    private BigDecimal qtLitros;

    @NotNull(message = "Valor do litro é obrigatório")
    @DecimalMin(value = "0.0001", message = "Valor do litro deve ser maior que zero")
    private BigDecimal valorLitro;

    @DecimalMin(value = "0.00", message = "Valor total deve ser >= 0")
    private BigDecimal valorTotal;

    @NotNull(message = "Tipo de combustível é obrigatório")
    private TipoCombustivel tipoCombustivel;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    @Size(max = 120, message = "Posto deve ter no máximo 120 caracteres")
    private String posto;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF inválida (ex: PB)")
    private String uf;

    @Size(max = 60, message = "Nº nota/cupom deve ter no máximo 60 caracteres")
    private String numNotaOuCupom;
}
