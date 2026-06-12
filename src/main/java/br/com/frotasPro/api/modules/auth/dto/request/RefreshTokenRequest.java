package br.com.frotasPro.api.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken é obrigatório")
        String refreshToken
) {
}
