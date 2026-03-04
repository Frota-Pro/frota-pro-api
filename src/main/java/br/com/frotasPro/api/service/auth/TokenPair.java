package br.com.frotasPro.api.service.auth;

public record TokenPair(
        String accessToken,
        long accessExpiresIn,
        String refreshToken,
        long refreshExpiresIn
) {
}
