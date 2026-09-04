package br.com.frotasPro.api.controller.integracao.dto;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record IntegracaoWinThorJobResponse(
        UUID jobId,
        String tipo,
        StatusSincronizacao status,
        LocalDate dataReferencia,
        Integer totalRegistros,
        String mensagemErro,
        /** "Automático", "Reforço mensal", "Manual" ou "Reprocessado" — só pra jobs de CARGAS; null pra caminhões/motoristas. */
        String origem,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {}
