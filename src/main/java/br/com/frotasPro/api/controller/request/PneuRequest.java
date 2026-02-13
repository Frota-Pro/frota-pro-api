package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PneuRequest {

    @NotBlank(message = "Número de série é obrigatório")
    @Size(min = 3, max = 60, message = "Número de série deve ter entre 3 e 60 caracteres")
    public String numeroSerie;

    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 60, message = "Marca deve ter entre 2 e 60 caracteres")
    public String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 2, max = 60, message = "Modelo deve ter entre 2 e 60 caracteres")
    public String modelo;

    @NotBlank(message = "Medida é obrigatória")
    @Size(min = 2, max = 30, message = "Medida deve ter entre 2 e 30 caracteres")
    public String medida;

    @PositiveOrZero(message = "Nível de recapagem deve ser >= 0")
    @Max(value = 10, message = "Nível de recapagem inválido")
    public Integer nivelRecapagem;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 30, message = "Status inválido")
    public String status;              // StatusPneu

    @DecimalMin(value = "0.00", message = "KM meta atual deve ser >= 0")
    public BigDecimal kmMetaAtual;     // meta configurável por pneu

    @NotNull(message = "Data de compra é obrigatória")
    public LocalDate dtCompra;
}
