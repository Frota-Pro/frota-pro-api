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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioLoginServiceTest {

    @Mock
    private BuscarUsuarioService buscarUsuarioService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthTokenService authTokenService;
    @Mock
    private LoginProtectionService loginProtectionService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RegistrarLogAuditoriaService auditoria;
    @InjectMocks
    private UsuarioLoginService service;

    private Usuario usuario;
    private final TokenPair tokens = new TokenPair("acesso", 900L, "refresh", 604_800L, false);

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNome("Vanderson Felix");
        usuario.setLogin("vanderson");
        usuario.setSenha("$2a$hash");
    }

    private LoginRequest request(String login, String senha) {
        LoginRequest r = new LoginRequest();
        r.setLogin(login);
        r.setSenha(senha);
        return r;
    }

    private void usuarioValido(String senhaDigitada) {
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        when(passwordEncoder.matches(senhaDigitada, "$2a$hash")).thenReturn(true);
        when(authTokenService.generateTokenPair(usuario)).thenReturn(tokens);
    }

    // ---- sucesso

    @Test
    void loginCorretoDevolveOsTokensERegistraOAcesso() {
        usuarioValido("segredo");

        LoginResponse r = service.login(request("vanderson", "segredo"), "10.0.0.5");

        assertEquals("acesso", r.accessToken());
        assertEquals(900L, r.expiresIn());
        assertEquals("refresh", r.refreshToken());
        assertEquals(604_800L, r.refreshExpiresIn());
        assertFalse(r.mustChangePassword());
        assertEquals(1, usuario.getTotalLogins());
        assertNotNull(usuario.getUltimoLoginEm());
        verify(usuarioRepository).save(usuario);
        verify(loginProtectionService).registerSuccess("10.0.0.5", "vanderson");
        verify(auditoria).registrar("vanderson", "Vanderson Felix", AcaoAuditoria.LOGIN_SUCESSO, null,
                "Login", "POST", "/login", 200, "10.0.0.5");
    }

    @Test
    void verificaALimitacaoDeTentativasAntesDeQualquerConsulta() {
        usuarioValido("segredo");

        service.login(request("vanderson", "segredo"), "10.0.0.5");

        InOrder ordem = Mockito.inOrder(loginProtectionService, buscarUsuarioService);
        ordem.verify(loginProtectionService).assertAllowed("10.0.0.5", "vanderson");
        ordem.verify(buscarUsuarioService).buscarUsuarioPorlogin("vanderson");
    }

    @Test
    void comparaASenhaDigitadaComOHashGuardado() {
        usuarioValido("segredo");

        service.login(request("vanderson", "segredo"), "ip");

        verify(passwordEncoder).matches("segredo", "$2a$hash");
    }

    @Test
    void naoGuardaUltimoLoginEmCasoDeFalha() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.login(request("vanderson", "errada"), "ip"));

        verify(usuarioRepository, never()).save(any());
        assertEquals(0, usuario.getTotalLogins());
    }

    // ---- normalização

    @Test
    void ignoraMaiusculasEEspacosNoLogin() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        when(passwordEncoder.matches("segredo", "$2a$hash")).thenReturn(true);
        when(authTokenService.generateTokenPair(usuario)).thenReturn(tokens);

        service.login(request("  VandersoN  ", "segredo"), "ip");

        verify(buscarUsuarioService).buscarUsuarioPorlogin("vanderson");
        verify(loginProtectionService).assertAllowed("ip", "vanderson");
    }

    @Test
    void loginNuloOuEmBrancoViraUnknownEIpNuloTambem() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("unknown"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "nao existe"));

        assertThrows(ResponseStatusException.class, () -> service.login(request(null, "x"), null));
        assertThrows(ResponseStatusException.class, () -> service.login(request("   ", "x"), "  "));

        verify(loginProtectionService, Mockito.times(2)).assertAllowed("unknown", "unknown");
    }

    // ---- falhas

    @Test
    void usuarioInexistenteOuInativoRecebe401ComMensagemGenerica() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("fantasma"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.login(request("fantasma", "x"), "10.0.0.5"));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        assertEquals("Credenciais inválidas", e.getReason());
        verify(loginProtectionService).registerFailure("10.0.0.5", "fantasma");
        verify(auditoria).registrar(eq("fantasma"), isNull(), eq(AcaoAuditoria.LOGIN_FALHA), isNull(),
                anyString(), eq("POST"), eq("/login"), eq(401), eq("10.0.0.5"));
        verifyNoInteractions(authTokenService, passwordEncoder);
    }

    @Test
    void senhaErradaRecebeAMesmaMensagemDeUsuarioInexistente() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        when(passwordEncoder.matches("errada", "$2a$hash")).thenReturn(false);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.login(request("vanderson", "errada"), "10.0.0.5"));

        // mesma resposta nos dois casos: nao revela se o usuario existe
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        assertEquals("Credenciais inválidas", e.getReason());
        verify(loginProtectionService).registerFailure("10.0.0.5", "vanderson");
        verify(loginProtectionService, never()).registerSuccess(anyString(), anyString());
        verify(auditoria).registrar(eq("vanderson"), eq("Vanderson Felix"), eq(AcaoAuditoria.LOGIN_FALHA), isNull(),
                anyString(), eq("POST"), eq("/login"), eq(401), eq("10.0.0.5"));
        verifyNoInteractions(authTokenService);
    }

    @Test
    void usuarioBloqueadoNemChegaAConsultarOBanco() {
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "bloqueado"))
                .when(loginProtectionService).assertAllowed("10.0.0.5", "vanderson");

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.login(request("vanderson", "segredo"), "10.0.0.5"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
        verifyNoInteractions(buscarUsuarioService, passwordEncoder, authTokenService, usuarioRepository);
    }

    // ---- senha padrão

    @Test
    void contaQueAindaUsaASenhaPadraoPassaAExigirTrocaDeSenha() {
        usuario.setSenhaTemporaria(false);
        usuarioValido("padrao123");

        service.login(request("vanderson", "padrao123"), "ip");

        // marcou antes de gerar os tokens, para o token ja sair com mustChangePassword
        ArgumentCaptor<Usuario> geradoPara = ArgumentCaptor.forClass(Usuario.class);
        verify(authTokenService).generateTokenPair(geradoPara.capture());
        assertTrue(geradoPara.getValue().isSenhaTemporaria());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void senhaDiferenteDaPadraoNaoMarcaTrocaObrigatoria() {
        usuario.setSenhaTemporaria(false);
        usuarioValido("outra-senha");

        service.login(request("vanderson", "outra-senha"), "ip");

        assertFalse(usuario.isSenhaTemporaria());
    }

    @Test
    void contaJaTemporariaContinuaTemporaria() {
        usuario.setSenhaTemporaria(true);
        usuarioValido("padrao123");

        service.login(request("vanderson", "padrao123"), "ip");

        assertTrue(usuario.isSenhaTemporaria());
    }

    @Test
    void oFlagDeSenhaTemporariaVoltaNaRespostaDoLogin() {
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        when(passwordEncoder.matches("segredo", "$2a$hash")).thenReturn(true);
        when(authTokenService.generateTokenPair(usuario)).thenReturn(new TokenPair("a", 1L, "r", 2L, true));

        assertTrue(service.login(request("vanderson", "segredo"), "ip").mustChangePassword());
    }
}
