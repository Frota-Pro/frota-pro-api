package br.com.frotasPro.api.controller.integracao.dto;

import jakarta.validation.constraints.Min;

public record IntegracaoWinThorConfigUpdateRequest(
        Boolean ativo,
        @Min(1) Integer intervaloMin,
        Boolean syncCaminhoes,
        Boolean syncMotoristas,
        Boolean syncCargas
) {}
