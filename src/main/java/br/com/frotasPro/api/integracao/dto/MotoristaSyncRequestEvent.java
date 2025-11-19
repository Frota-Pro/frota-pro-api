package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotoristaSyncRequestEvent {
    private UUID jobId;
    private UUID empresaId;
    private OffsetDateTime timestampSolicitacao;
}