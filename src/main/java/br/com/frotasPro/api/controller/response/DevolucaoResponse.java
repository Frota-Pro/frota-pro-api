package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Um item devolvido (uma linha de produto) de uma nota fiscal desta carga, buscado ao vivo no WinThor. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolucaoResponse {

    private Integer codDevolucao;
    private Long numNota;
    private Long numPedido;
    private OffsetDateTime dtEntrada;
    private String motivo;

    private Integer codCliente;
    private String nomeCliente;

    private Integer codProduto;
    private String descricaoProduto;
    private BigDecimal quantidade;
    private String unidade;
    private String embalagem;

    private BigDecimal valorDevolucao;
    private Double pesoTotalKg;

    private String fornecedor;
    private String nomeMotoristaDevolucao;
}
