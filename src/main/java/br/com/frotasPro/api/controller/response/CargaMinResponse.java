package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.config.json.BigDecimalBrasilSerializer;
import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.domain.enums.Status;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CargaMinResponse {

    private String numeroCarga;
    private String numeroCargaExterno;
    private LocalDate dtSaida;
    private LocalDate dtChegada;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal pesoCarga;
    @JsonSerialize(using = BigDecimalBrasilSerializer.class)
    private BigDecimal valorTotal;
    private Status statusCarga;
    private String nomeMotorista;
    private String placaCaminhao;
}
