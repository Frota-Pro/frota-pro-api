package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.request.RefreshTokenRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.service.auth.AuthTokenService;
import br.com.frotasPro.api.service.auth.TokenPair;
import br.com.frotasPro.api.service.usuario.UsuarioLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioLoginService usuarioLoginService;
    private final AuthTokenService authTokenService;

    @PostMapping
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return usuarioLoginService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenPair tokenPair = authTokenService.refresh(request.refreshToken());
        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.accessExpiresIn(),
                tokenPair.refreshToken(),
                tokenPair.refreshExpiresIn()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authTokenService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
