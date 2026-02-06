package br.com.frotasPro.api.controller.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class PneuRequest {
    public String numeroSerie;
    public String marca;
    public String modelo;
    public String medida;

    public Integer nivelRecapagem;
    public String status;              // StatusPneu

    public BigDecimal kmMetaAtual;     // meta configurável por pneu
    public LocalDate dtCompra;
}
