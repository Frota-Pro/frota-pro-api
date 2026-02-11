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
public class RelatorioHistoricoManutencaoResponse {

    private String codigoCaminhao;
    private String placaCaminhao;

    private LocalDate periodoInicio;
    private LocalDate periodoFim;

    private BigDecimal totalManutencao;

    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private LocalDate data;
        private String descricao;
        private String tipo;
        private BigDecimal valor;
        private String status;
    }
}
