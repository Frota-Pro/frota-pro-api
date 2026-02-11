package br.com.frotasPro.api.controller.integracao.dto;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record IntegracaoWinThorLogsResponse(
        String source,           // "API" | "INTEGRADORA"
        OffsetDateTime fetchedAt,
        int linesRequested,
        int linesReturned,
        List<String> lines
) {}
