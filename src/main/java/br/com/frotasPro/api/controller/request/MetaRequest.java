package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter
public class MetaRequest {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataIncio;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFim;
    private TipoMeta tipoMeta;
    private BigDecimal valorMeta;
    private BigDecimal valorRealizado;
    private String unidade;
    private StatusMeta statusMeta;
    private String descricao;

    private UUID caminhaoId;
    private UUID motoristaId;
}
