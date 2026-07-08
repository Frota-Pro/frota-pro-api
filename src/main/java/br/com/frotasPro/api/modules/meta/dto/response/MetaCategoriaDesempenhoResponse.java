package br.com.frotasPro.api.modules.meta.dto.response;

import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.shared.enums.TipoMeta;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MetaCategoriaDesempenhoResponse {

    private String categoriaCodigo;
    private String categoriaDescricao;
    private LocalDate dataReferencia;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private List<Linha> linhas;

    @Getter
    @Builder
    public static class Linha {
        private UUID metaId;
        private TipoMeta tipoMeta;
        private String regraAtingimento;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorMeta;
        private String unidade;
        private String caminhaoCodigo;
        private String caminhaoDescricao;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal valorRealizado;
        @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
        private BigDecimal percentual;
        private Boolean metaAtingida;
    }
}
