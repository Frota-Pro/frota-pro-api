package br.com.frotasPro.api.modules.auth.service;

public record TokenPair(
        String accessToken,
        long accessExpiresIn,
        String refreshToken,
        long refreshExpiresIn
) {
}
