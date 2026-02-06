package br.com.frotasPro.api.controller.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class PneuMovimentacaoRequest {

    public String tipo;            // TipoMovimentacaoPneu

    public BigDecimal kmEvento;
    public String observacao;

    public UUID caminhaoId;
    public UUID manutencaoId;
    public UUID paradaId;

    public Integer eixoNumero;
    public String lado;
    public String posicao;

    // usado para INSTALACAO (obrigatório)
    public BigDecimal kmInstalacao;
}
