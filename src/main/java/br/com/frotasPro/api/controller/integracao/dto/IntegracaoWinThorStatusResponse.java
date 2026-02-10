package br.com.frotasPro.api.controller.integracao.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record IntegracaoWinThorStatusResponse(
        boolean apiOk,
        boolean integradoraOk,
        boolean oracleOk,
        String integradoraStatus,
        String oracleStatus,
        Long latenciaMs,
        OffsetDateTime verificadoEm
) {}
