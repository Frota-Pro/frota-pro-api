package br.com.frotasPro.api.controller.response;

import java.time.OffsetDateTime;

public record BiEmbedUrlResponse(int dashboardId, String url, OffsetDateTime expiraEm) {
}
