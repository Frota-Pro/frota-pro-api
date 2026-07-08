package br.com.frotasPro.api.modules.integracao.dto.request;

import jakarta.validation.constraints.Min;

import java.util.List;

public record IntegracaoWinThorConfigUpdateRequest(
        Boolean ativo,
        @Min(1) Integer intervaloMin,
        Boolean syncCaminhoes,
        Boolean syncMotoristas,
        Boolean syncCargas,
        List<Integer> codigosCaminhoes,
        List<Integer> codigosMotoristas
) {}
