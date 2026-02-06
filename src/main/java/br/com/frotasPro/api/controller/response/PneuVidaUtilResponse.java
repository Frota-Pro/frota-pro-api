package br.com.frotasPro.api.controller.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class PneuVidaUtilResponse {

    public String codigoPneu;
    public String status;
    public Integer nivelRecapagem;

    public BigDecimal kmMetaAtual;
    public BigDecimal kmRodadoAtual;
    public BigDecimal kmRestante;
    public BigDecimal percentualVida;      // 0..1

    public BigDecimal kmTotalAcumulado;

    public String caminhaoAtual;           // aqui vou devolver o UUID em string (se quiser, depois buscamos descrição/placa)
    public Integer eixoNumero;
    public String lado;
    public String posicao;

    public BigDecimal kmInstalacao;
    public LocalDateTime dataInstalacao;
}
