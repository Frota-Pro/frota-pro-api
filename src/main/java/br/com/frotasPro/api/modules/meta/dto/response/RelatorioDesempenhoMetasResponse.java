package br.com.frotasPro.api.modules.meta.dto.response;

import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.shared.enums.TipoMeta;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class RelatorioDesempenhoMetasResponse {

    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private TipoMeta tipoMeta;
    private String filtroCaminhao;
    private String filtroMotorista;
    private String filtroCategoria;
    private Long totalLinhas;
    private Long totalDentroMeta;
    private Long totalForaMeta;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal percentualSucesso;
    private List<Linha> linhas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linha {
        private UUID metaId;
        private TipoMeta tipoMeta;
        private String descricaoMeta;
        private String alvoTipo;
        private String origemMeta;
        private String origemMetaDescricao;
        private String caminhaoCodigo;
        private String caminhaoDescricao;
        private String motoristaCodigo;
        private String motoristaNome;
        private String categoriaCodigo;
        private String categoriaDescricao;
        private String regraAtingimento;
        private String regraAtingimentoTexto;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorMeta;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorRealizado;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal percentual;
        private String unidade;
        private Boolean metaAtingida;
        private String status;
        private LocalDate periodoCalculoInicio;
        private LocalDate periodoCalculoFim;
    }
}
