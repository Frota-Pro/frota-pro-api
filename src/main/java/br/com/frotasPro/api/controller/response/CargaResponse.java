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
public class CargaResponse {

    private UUID id;
    private String numeroCarga;
    private String numeroCargaExterno;

    private LocalDate dtSaida;
    private LocalDate dtPrevista;
    private LocalDate dtChegada;

    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal pesoCarga;

    @JsonSerialize(using = BigDecimalBrasilSerializer.class)
    private BigDecimal valorTotal;

    private Integer kmInicial;
    private Integer kmFinal;
    private Integer kmTotal;

    private long diasAtraso;

    private List<ClienteCargaResponse> clientes;

    private Status statusCarga;

    private String codigoMotorista;
    private String nomeMotorista;

    private String codigoCaminhao;
    private String placaCaminhao;

    private String codigoRota;

    private List<String> codigosAjudantes;

    // ===== NOVOS CAMPOS =====
    /** Ordem definida para entrega (lista ordenada de clientes) */
    private List<String> ordemEntregaClientes;

    /** Observação informada pelo motorista durante/ao final da carga */
    private String observacaoMotorista;
}