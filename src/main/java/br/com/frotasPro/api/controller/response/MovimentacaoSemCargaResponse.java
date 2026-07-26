package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimentacaoSemCargaResponse {

    private UUID id;
    private LocalDate dataMovimentacao;
    private String codigoCaminhao;
    private String placaCaminhao;
    private String numeroCargaInicio;
    /** Número a ser exibido ao usuário: externo se a integração estiver ativa, senão interno. */
    private String numeroCargaInicioExibicao;
    private Integer kmOrigem;
    private Integer kmDestino;
    private Integer kmRodado;
    private BigDecimal mediaKmLitroUsada;
    private BigDecimal valorLitroMedio;
    private BigDecimal custoEstimado;
}
