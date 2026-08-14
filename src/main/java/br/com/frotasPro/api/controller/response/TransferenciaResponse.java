package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Um registro de transferência de pedido entre carregamentos, buscado ao vivo no WinThor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaResponse {

    private Long numNota;
    private Integer numCarAtual;
    private Integer numCarAnterior;
    private OffsetDateTime dtTransferencia;
    private String motivo;

    /** "PERDIDA" (essa carga perdeu o pedido) ou "RECEBIDA" (essa carga recebeu o pedido). */
    private String direcao;

    /** Peso (kg) e valor (R$) da nota transferida. */
    private BigDecimal pesoKg;
    private BigDecimal valorTotal;
}
