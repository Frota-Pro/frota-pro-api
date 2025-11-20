package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargaSyncResponseEvent {

    private UUID jobId;
    private UUID empresaId;

    private LocalDate dataReferencia;
    private Integer totalCargas;

    private List<CargaWinThorDto> cargas;

    private OffsetDateTime timestampProcessado;
}
