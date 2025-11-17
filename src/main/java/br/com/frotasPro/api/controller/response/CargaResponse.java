package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.Status;
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
public class CargaResponse {

    private UUID id;
    private String numeroCarga;
    private String numeroCargaExterno;

    private LocalDate dtSaida;
    private LocalDate dtPrevista;
    private LocalDate dtChegada;

    private BigDecimal pesoCarga;

    private Integer kmInicial;
    private Integer kmFinal;
    private Integer kmTotal;

    private long diasAtraso;

    private List<String> clientes;
    private List<String> notas;

    private Status statusCarga;

    private String codigoMotorista;
    private String nomeMotorista;

    private String codigoCaminhao;
    private String placaCaminhao;

    private String codigoRota;

    private List<String> codigosAjudantes;
}
