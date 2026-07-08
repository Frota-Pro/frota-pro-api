package br.com.frotasPro.api.modules.meta.dto.response;

import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.shared.enums.StatusMeta;
import br.com.frotasPro.api.shared.enums.TipoMeta;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class MetaResponse {

    private UUID id;
    private LocalDate dataIncio;
    private LocalDate dataFim;
    private TipoMeta tipoMeta;
    private String regraAtingimento;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal valorMeta;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal valorRealizado;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal percentual;
    private Boolean metaAtingida;
    private String statusDesempenho;
    private String unidade;
    private StatusMeta statusMeta;
    private String descricao;

    private String caminhaoCodigo;
    private String caminhaoDescricao;

    private String categoriaCodigo;
    private String categoriaDescricao;

    private String motoristaCodigo;
    private String motoristaDescricao;

    private boolean renovarAutomaticamente;
}
