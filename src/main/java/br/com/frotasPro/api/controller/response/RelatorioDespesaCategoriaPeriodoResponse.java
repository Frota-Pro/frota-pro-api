package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioDespesaCategoriaPeriodoResponse {
    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private BigDecimal totalFrota;
    private BigDecimal totalPessoal;
    private BigDecimal totalGeral;

    private BigDecimal totalAbastecimento;
    private BigDecimal totalManutencoes;
    private BigDecimal totalPneu;
    private BigDecimal totalAlimentacao;
    private BigDecimal totalPernoite;

    private Integer quantidadeLancamentos;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private LocalDate data;
        private String categoriaPrincipal;
        private String subcategoria;
        private String referencia;
        private String descricao;
        private String cidade;
        private BigDecimal valor;
    }
}
