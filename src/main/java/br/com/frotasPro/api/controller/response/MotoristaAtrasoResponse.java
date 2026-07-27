package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotoristaAtrasoResponse {

    private String codigoMotorista;
    private String nomeMotorista;

    private long totalCargas;
    private long cargasAtrasoInicio;
    private long cargasAtrasoFim;

    private BigDecimal mediaAtrasoInicioDias;
    private BigDecimal mediaAtrasoFimDias;
}
