package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CargaRequest {

    @NotNull(message = "Data de saída é obrigatória")
    private LocalDate dtSaida;

    @NotNull(message = "Data prevista é obrigatória")
    private LocalDate dtPrevista;

    private LocalDate dtChegada;

    @DecimalMin(value = "0.00", message = "Peso da carga deve ser >= 0")
    private BigDecimal pesoCarga;

    @DecimalMin(value = "0.00", message = "Valor total deve ser >= 0")
    private BigDecimal valorTotal;

    @PositiveOrZero(message = "KM inicial deve ser >= 0")
    private Integer kmInicial;

    @PositiveOrZero(message = "KM final deve ser >= 0")
    private Integer kmFinal;

    private Status statusCarga = Status.EM_ROTA;

    @NotBlank(message = "Motorista é obrigatório")
    @Size(max = 80, message = "Motorista inválido")
    private String codigoMotorista;

    @NotBlank(message = "Caminhão é obrigatório")
    @Size(max = 80, message = "Caminhão inválido")
    private String codigoCaminhao;

    @NotBlank(message = "Rota é obrigatória")
    @Size(max = 80, message = "Rota inválida")
    private String codigoRota;

    private List<@NotBlank(message = "Ajudante inválido") String> codigosAjudantes;
}
