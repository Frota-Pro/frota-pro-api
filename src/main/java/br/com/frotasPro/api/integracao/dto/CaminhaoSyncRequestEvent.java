package br.com.frotasPro.api.integracao.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminhaoSyncRequestEvent {

    private UUID jobId;
    private UUID empresaId;
    private Integer codFilial;
    private OffsetDateTime timestampSolicitacao;
}
