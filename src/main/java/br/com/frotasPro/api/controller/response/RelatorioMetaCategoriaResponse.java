package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.TipoMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioMetaCategoriaResponse {

    private String categoriaCodigo;
    private String categoriaDescricao;
    private LocalDate dataReferencia;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private Long totalLinhas;
    private Long totalDentroMeta;
    private Long totalForaMeta;
    private BigDecimal percentualSucesso;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private UUID metaId;
        private TipoMeta tipoMeta;
        private String regraAtingimento;
        private String regraAtingimentoTexto;
        private BigDecimal valorMeta;
        private String unidade;
        private String caminhaoCodigo;
        private String caminhaoDescricao;
        private BigDecimal valorRealizado;
        private BigDecimal percentual;
        private Boolean metaAtingida;
        private String status;
    }
}
