package br.com.frotasPro.api.modules.dashboard.dto.response;

import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.shared.enums.TipoMeta;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DashboardMetasResponse {

    private long metasAtivas;
    private long metasVencendo;
    private long caminhoesForaMeta;
    private List<CategoriaResumo> categoriasPiorDesempenho;
    private List<CaminhaoResumo> topCaminhoesDentroMeta;

    @Getter
    @Builder
    public static class CategoriaResumo {
        private String categoriaCodigo;
        private String categoriaDescricao;
        private long totalResultados;
        private long totalForaMeta;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal percentualForaMeta;
    }

    @Getter
    @Builder
    public static class CaminhaoResumo {
        private String caminhaoCodigo;
        private String caminhaoDescricao;
        private TipoMeta tipoMeta;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorMeta;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorRealizado;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal percentual;
    }
}
