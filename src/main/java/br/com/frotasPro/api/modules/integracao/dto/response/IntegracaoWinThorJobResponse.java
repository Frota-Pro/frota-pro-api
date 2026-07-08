package br.com.frotasPro.api.modules.integracao.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import br.com.frotasPro.api.shared.enums.StatusSincronizacao;

@Builder
public record IntegracaoWinThorJobResponse(
        UUID jobId,
        String tipo,
        StatusSincronizacao status,
        LocalDate dataReferencia,
        Integer totalRegistros,
        String mensagemErro,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {}
