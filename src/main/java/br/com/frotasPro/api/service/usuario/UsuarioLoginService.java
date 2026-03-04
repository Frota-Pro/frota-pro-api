package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.service.auth.AuthTokenService;
import br.com.frotasPro.api.service.auth.LoginProtectionService;
import br.com.frotasPro.api.service.auth.TokenPair;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Slf4j
public class UsuarioLoginService {

    private final BuscarUsuarioService buscarUsuarioService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final LoginProtectionService loginProtectionService;

    public LoginResponse login(LoginRequest request, String clientIp) {
        String loginNormalizado = normalizeLogin(request.getLogin());
        String ip = normalizeIp(clientIp);

        loginProtectionService.assertAllowed(ip, loginNormalizado);

        Usuario usuario;
        try {
            usuario = buscarUsuarioService.buscarUsuarioPorlogin(loginNormalizado);
        } catch (ResponseStatusException ex) {
            loginProtectionService.registerFailure(ip, loginNormalizado);
            log.warn("security_event=login_failed reason=user_not_found_or_inactive login={} ip={}", loginNormalizado, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            loginProtectionService.registerFailure(ip, loginNormalizado);
            log.warn("security_event=login_failed reason=invalid_credentials login={} ip={}", loginNormalizado, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        TokenPair tokenPair = authTokenService.generateTokenPair(usuario);
        loginProtectionService.registerSuccess(ip, loginNormalizado);
        log.info("security_event=login_success login={} ip={}", loginNormalizado, ip);

        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.accessExpiresIn(),
                tokenPair.refreshToken(),
                tokenPair.refreshExpiresIn()
        );
    }

    private String normalizeLogin(String login) {
        if (login == null) {
            return "unknown";
        }
        String normalized = login.trim().toLowerCase();
        if (normalized.isBlank()) {
            return "unknown";
        }
        return normalized;
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }
}
