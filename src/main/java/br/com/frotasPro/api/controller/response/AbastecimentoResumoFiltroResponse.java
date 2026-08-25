package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Agregado da tela de Abastecimentos — soma tudo que bate com o filtro
 * aplicado (todas as páginas), não só a página atual. Ver
 * {@link br.com.frotasPro.api.repository.AbastecimentoRepository#resumoFiltradoNative}.
 */
@Getter
@Builder
@AllArgsConstructor
public class AbastecimentoResumoFiltroResponse {
    private BigDecimal totalLitros;
    private BigDecimal totalValor;
    private BigDecimal precoMedioLitro;
    private BigDecimal consumoMedioPonderado;
    private long totalRegistros;
}
