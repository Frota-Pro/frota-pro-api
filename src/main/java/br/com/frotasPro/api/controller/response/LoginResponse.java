package br.com.frotasPro.api.controller.response;

public record LoginResponse(String accessToken, Long expiresIn) {
}
