package br.com.frotasPro.api.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RelatorioManutencaoCaminhaoResponse {

    private String codigoCaminhao;
    private String caminhao;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    private Long quantidadeManutencoes;
    private Long quantidadePreventiva;
    private Long quantidadeCorretiva;

    private BigDecimal valorTotal;
    private BigDecimal valorMedio;
}
