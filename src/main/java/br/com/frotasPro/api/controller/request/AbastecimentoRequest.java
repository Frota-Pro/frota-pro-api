package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AbastecimentoRequest {

    @NotBlank(message = "Caminhão é obrigatório")
    @Size(max = 80, message = "Caminhão inválido")
    private String caminhao;

    @Size(max = 80, message = "Motorista inválido")
    private String motorista;

    @NotNull(message = "Data do abastecimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtAbastecimento;

    @PositiveOrZero(message = "KM do odômetro deve ser >= 0")
    private Integer kmOdometro;

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
