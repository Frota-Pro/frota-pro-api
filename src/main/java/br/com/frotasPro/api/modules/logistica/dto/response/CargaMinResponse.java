package br.com.frotasPro.api.modules.logistica.dto.response;

import br.com.frotasPro.api.config.json.BigDecimalBrasilSerializer;
import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.config.json.FlexibleBigDecimalDeserializer;
import br.com.frotasPro.api.shared.enums.Status;
import br.com.frotasPro.api.shared.enums.StatusTransferenciaCarga;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    @JsonSerialize(using = BigDecimalBrasilSerializer.class)
    private BigDecimal valorTotal;
    private Status statusCarga;
    private boolean transferenciaPendente;
    private StatusTransferenciaCarga statusTransferencia;
    private String nomeMotorista;
    private String placaCaminhao;
}
