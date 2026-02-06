package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class PneuResponse {
    public String codigo;
    public String numeroSerie;
    public String marca;
    public String modelo;
    public String medida;

    public Integer nivelRecapagem;
    public String status;

    public BigDecimal kmMetaAtual;
    public BigDecimal kmTotalAcumulado;

    public LocalDate dtCompra;
    public LocalDate dtDescarte;
}
