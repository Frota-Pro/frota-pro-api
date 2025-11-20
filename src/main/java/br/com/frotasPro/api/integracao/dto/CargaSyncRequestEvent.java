package br.com.frotasPro.api.integracao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargaSyncRequestEvent {

    private UUID jobId;
    private UUID empresaId;

    private LocalDate dataInicial;
    private LocalDate dataFinal;

    private String tipoCarga;
    private String origem;
    private String solicitadoPor;

    private OffsetDateTime timestampSolicitacao;
}
