package br.com.frotasPro.api.modules.integracao.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import br.com.frotasPro.api.modules.integracao.dto.CaminhaoWinThorDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaminhaoSyncResponseEvent {

    private UUID jobId;
    private UUID empresaId;
    private List<CaminhaoWinThorDto> caminhoes;
    private OffsetDateTime timestampProcessado;
}
