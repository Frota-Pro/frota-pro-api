package br.com.frotasPro.api.integracao.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminhaoWinThorDto {

    private Integer codVeiculo;
    private String placa;
    private String descricao;
    private String marca;
    private BigDecimal pesoMaximoKg;
    private String situacao;
}
