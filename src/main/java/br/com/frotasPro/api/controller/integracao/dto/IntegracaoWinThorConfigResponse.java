package br.com.frotasPro.api.controller.integracao.dto;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record IntegracaoWinThorConfigResponse(
        UUID empresaId,
        boolean ativo,
        Integer intervaloMin,
        boolean syncCaminhoes,
        boolean syncMotoristas,
        boolean syncCargas,
        List<Integer> codigosCaminhoes,
        List<Integer> codigosMotoristas,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {}
