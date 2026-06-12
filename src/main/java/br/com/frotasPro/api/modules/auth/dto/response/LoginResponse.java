package br.com.frotasPro.api.modules.auth.dto.response;

public record LoginResponse(
        String accessToken,
        Long expiresIn,
        String refreshToken,
        Long refreshExpiresIn
) {
}
