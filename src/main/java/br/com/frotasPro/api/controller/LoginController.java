package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.util.ClientIp;
import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.request.RefreshTokenRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.service.auth.AuthTokenService;
import br.com.frotasPro.api.service.auth.TokenPair;
import br.com.frotasPro.api.service.usuario.UsuarioLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/login", "/auth/login"})
@RequiredArgsConstructor
public class LoginController {

    private final UsuarioLoginService usuarioLoginService;
    private final AuthTokenService authTokenService;

    @PostMapping
    public LoginResponse login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        return usuarioLoginService.login(request, extractClientIp(httpRequest));
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenPair tokenPair = authTokenService.refresh(request.refreshToken());
        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.accessExpiresIn(),
                tokenPair.refreshToken(),
                tokenPair.refreshExpiresIn(),
                tokenPair.mustChangePassword()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authTokenService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private String extractClientIp(HttpServletRequest request) {
        return ClientIp.de(request);
    }
}
