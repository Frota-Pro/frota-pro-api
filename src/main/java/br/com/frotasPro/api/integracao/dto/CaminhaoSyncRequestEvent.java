package br.com.frotasPro.api.integracao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminhaoSyncRequestEvent {

    private UUID jobId;
    private UUID empresaId;
    private Integer codFilial;
    private List<Integer> codigosCaminhoes;
    private OffsetDateTime timestampSolicitacao;
}
