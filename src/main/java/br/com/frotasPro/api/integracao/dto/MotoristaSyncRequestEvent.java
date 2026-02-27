package br.com.frotasPro.api.integracao.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotoristaSyncRequestEvent {
    private UUID jobId;
    private UUID empresaId;
    private List<Integer> codigosMotoristas;
    private OffsetDateTime timestampSolicitacao;
}
