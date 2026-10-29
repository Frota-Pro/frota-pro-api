package br.com.frotasPro.api.service.usuario;

import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.repository.UsuarioRepository;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
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

    private static final String SENHA_PADRAO = "padrao123";

    private final BuscarUsuarioService buscarUsuarioService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final LoginProtectionService loginProtectionService;
    private final UsuarioRepository usuarioRepository;
    private final RegistrarLogAuditoriaService registrarLogAuditoriaService;
    private final SenhaPadraoPolitica senhaPadraoPolitica;
    private final VinculoDispositivoService vinculoDispositivoService;

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
            registrarLogAuditoriaService.registrar(loginNormalizado, null, AcaoAuditoria.LOGIN_FALHA, null,
                    "Tentativa de login com usuário inexistente ou inativo", "POST", "/login", 401, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            loginProtectionService.registerFailure(ip, loginNormalizado);
            log.warn("security_event=login_failed reason=invalid_credentials login={} ip={}", loginNormalizado, ip);
            registrarLogAuditoriaService.registrar(loginNormalizado, usuario.getNome(), AcaoAuditoria.LOGIN_FALHA, null,
                    "Tentativa de login com senha incorreta", "POST", "/login", 401, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        if (senhaPadraoPolitica.deveBloquear(request.getSenha())) {
            log.warn("security_event=login_bloqueado_senha_padrao login={} ip={}", loginNormalizado, ip);
            registrarLogAuditoriaService.registrar(loginNormalizado, usuario.getNome(), AcaoAuditoria.LOGIN_FALHA, null,
                    "Login recusado: conta ainda usa a senha padrão", "POST", "/login", 403, ip);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Esta conta ainda usa a senha padrão, que foi desativada. Peça ao administrador para redefinir sua senha.");
        }

        // Detecta contas antigas que nunca trocaram a senha padrão (ex: admin
        // e motoristas criados em massa) e passa a exigir a troca daqui pra frente.
        if (SENHA_PADRAO.equals(request.getSenha()) && !usuario.isSenhaTemporaria()) {
            usuario.setSenhaTemporaria(true);
            log.warn("security_event=senha_padrao_detectada login={}", loginNormalizado);
        }

        // motorista só entra pelo celular vinculado (null = o vínculo não se aplica a esta conta)
        String dispositivoHash = vinculoDispositivoService.validarNoLogin(
                usuario, request.getDispositivoId(), request.getDispositivoNome(), ip);

        TokenPair tokenPair = dispositivoHash == null
                ? authTokenService.generateTokenPair(usuario)
                : authTokenService.generateTokenPair(usuario, dispositivoHash);
        loginProtectionService.registerSuccess(ip, loginNormalizado);
        log.info("security_event=login_success login={} ip={}", loginNormalizado, ip);
        registrarLogAuditoriaService.registrar(loginNormalizado, usuario.getNome(), AcaoAuditoria.LOGIN_SUCESSO, null,
                "Login", "POST", "/login", 200, ip);

        usuario.registrarLogin();
        usuarioRepository.save(usuario);

        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.accessExpiresIn(),
                tokenPair.refreshToken(),
                tokenPair.refreshExpiresIn(),
                tokenPair.mustChangePassword()
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
