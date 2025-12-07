package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinhaRelatorioMetaMensalMotoristaResponse {

    private LocalDate data;
    private String lote;
    private String cidade;
    private BigDecimal valorCarga;
    private BigDecimal tonelagem;

    private Integer kmInicial;
    private Integer kmFinal;
    private Long kmRodado;

    private BigDecimal litros;
    private BigDecimal valorAbastecimento;

    private BigDecimal mediaKmLitro;
}
