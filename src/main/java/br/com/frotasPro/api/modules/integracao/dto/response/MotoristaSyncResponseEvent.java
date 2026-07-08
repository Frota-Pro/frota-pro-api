package br.com.frotasPro.api.modules.integracao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import br.com.frotasPro.api.modules.integracao.dto.MotoristaWinThorDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotoristaSyncResponseEvent {
    private UUID jobId;
    private UUID empresaId;
    private List<MotoristaWinThorDto> motoristas;
    private OffsetDateTime timestampProcessado;
}
