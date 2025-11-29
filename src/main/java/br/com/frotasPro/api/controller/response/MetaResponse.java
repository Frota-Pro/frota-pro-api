package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
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
    private BigDecimal valorMeta;
    private BigDecimal valorRealizado;
    private String unidade;
    private StatusMeta statusMeta;
    private String descricao;

    private String caminhaoCodigo;
    private String caminhaoDescricao;

    private String categoriaCodigo;
    private String categoriaDescricao;

    private String motoristaCodigo;
    private String motoristaDescricao;
}
