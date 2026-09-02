package br.com.frotasPro.api.controller.integracao.dto;

import jakarta.validation.constraints.Min;

import java.time.LocalTime;
import java.util.List;

public record IntegracaoWinThorConfigUpdateRequest(
        Boolean ativo,
        @Min(1) Integer intervaloMin,
        LocalTime horarioReforcoMensal,
        Boolean syncCaminhoes,
        Boolean syncMotoristas,
        Boolean syncCargas,
        List<Integer> codigosCaminhoes,
        List<Integer> codigosMotoristas
) {}
