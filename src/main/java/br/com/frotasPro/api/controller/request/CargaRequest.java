package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CargaRequest {

    private LocalDate dtSaida;
    private LocalDate dtPrevista;
    private LocalDate dtChegada;

    private BigDecimal pesoCarga;

    private Integer kmInicial;
    private Integer kmFinal;

    private Status statusCarga = Status.EM_ROTA;

    @NotBlank
    private String codigoMotorista;

    @NotBlank
    private String codigoCaminhao;

    @NotBlank
    private String codigoRota;

    private List<String> codigosAjudantes;
}
