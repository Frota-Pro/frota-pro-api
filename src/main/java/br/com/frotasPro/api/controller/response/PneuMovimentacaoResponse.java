package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PneuMovimentacaoResponse {
    public UUID id;
    public String tipo;
    public LocalDateTime dataEvento;
    public BigDecimal kmEvento;
    public String observacao;
    public UUID caminhaoId;
    public UUID manutencaoId;
    public UUID paradaId;
    public Integer eixoNumero;
    public String lado;
    public String posicao;
    public LocalDateTime criadoEm;
}
