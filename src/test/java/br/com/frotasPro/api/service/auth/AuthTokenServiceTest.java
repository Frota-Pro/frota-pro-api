package br.com.frotasPro.api.service.auth;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
import br.com.frotasPro.api.service.usuario.BuscarUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> ops;
    @Mock
    private BuscarUsuarioService buscarUsuarioService;
    @Mock
    private RegistrarLogAuditoriaService auditoria;
    @Mock
    private br.com.frotasPro.api.service.usuario.VinculoDispositivoService vinculoDispositivo;

    private AuthTokenService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new AuthTokenService(jwtEncoder, jwtDecoder, redis, buscarUsuarioService, auditoria, vinculoDispositivo);
        ReflectionTestUtils.setField(service, "accessTokenSeconds", 900L);
        ReflectionTestUtils.setField(service, "refreshTokenSeconds", 604_800L);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Vanderson Felix");
        usuario.setLogin("vanderson");
        Acesso motorista = new Acesso();
        motorista.setNome("ROLE_MOTORISTA");
        usuario.setAcesso(new ArrayList<>(List.of(motorista)));

        lenient().when(redis.opsForValue()).thenReturn(ops);
        lenient().when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(inv -> {
            JwtEncoderParameters p = inv.getArgument(0);
            return Jwt.withTokenValue("token-" + p.getClaims().getClaimAsString("token_type"))
                    .header("alg", "RS256")
                    .claims(c -> c.putAll(p.getClaims().getClaims()))
                    .build();
        });
    }

    private Jwt refreshToken(String tipo, String jti, String login, Instant expira) {
        Jwt.Builder b = Jwt.withTokenValue("r").header("alg", "RS256").claim("token_type", tipo)
                .subject("Vanderson Felix");
        if (jti != null) b.jti(jti);
        if (login != null) b.claim("login", login);
        if (expira != null) b.expiresAt(expira).issuedAt(Instant.now().minusSeconds(10));
        else b.claim("sem_exp", true);
        return b.build();
    }

    private ResponseStatusException negado(Runnable acao) {
        ResponseStatusException e = assertThrows(ResponseStatusException.class, acao::run);
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        return e;
    }

    // ---- geração do par de tokens

    @Test
    void geraAccessERefreshComAsValidadesConfiguradas() {
        TokenPair par = service.generateTokenPair(usuario);

        assertEquals("token-access", par.accessToken());
        assertEquals("token-refresh", par.refreshToken());
        assertEquals(900L, par.accessExpiresIn());
        assertEquals(604_800L, par.refreshExpiresIn());
        assertFalse(par.mustChangePassword());
    }

    @Test
    void oAccessTraLoginPerfisEFlagDeSenhaTemporaria() {
        usuario.setSenhaTemporaria(true);

        TokenPair par = service.generateTokenPair(usuario);

        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        var access = captura.getAllValues().get(0).getClaims();
        assertEquals("access", access.getClaimAsString("token_type"));
        assertEquals("vanderson", access.getClaimAsString("login"));
        assertEquals(List.of("ROLE_MOTORISTA"), access.getClaim("scope"));
        assertEquals(true, access.getClaim("mustChangePassword"));
        assertEquals("frotaPro-api", access.getClaimAsString("iss"));
        assertTrue(par.mustChangePassword());
    }

    @Test
    void oRefreshNaoCarregaPerfisEEUmTokenDeOutroTipo() {
        service.generateTokenPair(usuario);

        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        var refresh = captura.getAllValues().get(1).getClaims();
        assertEquals("refresh", refresh.getClaimAsString("token_type"));
        assertEquals("vanderson", refresh.getClaimAsString("login"));
        assertNull(refresh.getClaim("scope"));
    }

    @Test
    void cadaTokenTemUmIdentificadorProprio() {
        service.generateTokenPair(usuario);

        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        String jtiAccess = captura.getAllValues().get(0).getClaims().getId();
        String jtiRefresh = captura.getAllValues().get(1).getClaims().getId();
        assertTrue(jtiAccess != null && !jtiAccess.isBlank());
        assertTrue(!jtiAccess.equals(jtiRefresh));
    }

    // ---- renovação

    @Test
    void renovarRevogaORefreshAntigoEEmiteUmParNovo() {
        Jwt antigo = refreshToken("refresh", "jti-1", "vanderson", Instant.now().plusSeconds(3600));
        when(jwtDecoder.decode("refresh-antigo")).thenReturn(antigo);
        when(redis.hasKey("auth:revoked:jti-1")).thenReturn(false);
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);

        TokenPair novo = service.refresh("refresh-antigo");

        assertEquals("token-access", novo.accessToken());
        verify(ops).set(eq("auth:revoked:jti-1"), eq("1"), any(Duration.class));
    }

    @Test
    void refreshJaRevogadoEhRecusadoESemEmitirNada() {
        Jwt antigo = refreshToken("refresh", "jti-1", "vanderson", Instant.now().plusSeconds(3600));
        when(jwtDecoder.decode("r")).thenReturn(antigo);
        when(redis.hasKey("auth:revoked:jti-1")).thenReturn(true);

        ResponseStatusException e = negado(() -> service.refresh("r"));

        assertEquals("Token já revogado", e.getReason());
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    void accessTokenNaoServeParaRenovar() {
        when(jwtDecoder.decode("a")).thenReturn(refreshToken("access", "jti-9", "vanderson", Instant.now().plusSeconds(60)));

        ResponseStatusException e = negado(() -> service.refresh("a"));

        assertEquals("Token inválido para esta operação", e.getReason());
        verifyNoInteractions(buscarUsuarioService);
    }

    @Test
    void tokenQueNaoSeDecodificaEhInvalido() {
        when(jwtDecoder.decode("lixo")).thenThrow(new IllegalArgumentException("assinatura ruim"));

        ResponseStatusException e = negado(() -> service.refresh("lixo"));

        assertEquals("Token inválido", e.getReason());
    }

    @Test
    void refreshSemIdentificadorEhInvalido() {
        when(jwtDecoder.decode("r")).thenReturn(refreshToken("refresh", null, "vanderson", Instant.now().plusSeconds(60)));

        negado(() -> service.refresh("r"));
    }

    @Test
    void refreshSemLoginNoTokenEhInvalidoMasJaFoiRevogado() {
        when(jwtDecoder.decode("r")).thenReturn(refreshToken("refresh", "jti-2", null, Instant.now().plusSeconds(3600)));
        when(redis.hasKey("auth:revoked:jti-2")).thenReturn(false);

        ResponseStatusException e = negado(() -> service.refresh("r"));

        assertEquals("Refresh token inválido", e.getReason());
        // o token e queimado antes da checagem do login: nao pode ser reutilizado
        verify(ops).set(eq("auth:revoked:jti-2"), eq("1"), any(Duration.class));
    }

    @Test
    void refreshJaExpiradoNaoGravaRevogacaoNoRedis() {
        Jwt expirado = refreshToken("refresh", "jti-3", "vanderson", Instant.now().minusSeconds(5));
        when(jwtDecoder.decode("r")).thenReturn(expirado);
        when(redis.hasKey("auth:revoked:jti-3")).thenReturn(false);
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);

        service.refresh("r");

        verify(ops, never()).set(anyString(), anyString(), any(Duration.class));
    }

    // ---- logout

    @Test
    void logoutRevogaOTokenEAudita() {
        Jwt token = refreshToken("refresh", "jti-4", "vanderson", Instant.now().plusSeconds(3600));
        when(jwtDecoder.decode("r")).thenReturn(token);
        when(redis.hasKey("auth:revoked:jti-4")).thenReturn(false);

        service.logout("r");

        verify(ops).set(eq("auth:revoked:jti-4"), eq("1"), any(Duration.class));
        verify(auditoria).registrar(eq("vanderson"), eq("Vanderson Felix"), eq(AcaoAuditoria.LOGOUT),
                eq(null), eq("Logout"), eq("POST"), eq("/login/logout"), eq(204), eq(null));
    }

    @Test
    void logoutComTokenJaRevogadoEhRecusadoESemAuditar() {
        Jwt token = refreshToken("refresh", "jti-4", "vanderson", Instant.now().plusSeconds(3600));
        when(jwtDecoder.decode("r")).thenReturn(token);
        when(redis.hasKey("auth:revoked:jti-4")).thenReturn(true);

        negado(() -> service.logout("r"));

        verifyNoInteractions(auditoria);
    }

    @Test
    void logoutComAccessTokenEhRecusado() {
        when(jwtDecoder.decode("a")).thenReturn(refreshToken("access", "jti-5", "vanderson", Instant.now().plusSeconds(60)));

        negado(() -> service.logout("a"));

        verifyNoInteractions(auditoria);
        verify(redis, never()).hasKey(anyString());
    }

    // ---- vinculo do aparelho no refresh token

    @Test
    void refreshTokenDeSessaoVinculadaCarregaOHashDoAparelho() {
        service.generateTokenPair(usuario, "hash-do-celular");

        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        assertEquals("hash-do-celular", captura.getAllValues().get(1).getClaims().getClaimAsString("dev"));
        assertNull(captura.getAllValues().get(0).getClaims().getClaim("dev"));
    }

    @Test
    void semVinculoORefreshTokenNaoTemClaimDeAparelho() {
        service.generateTokenPair(usuario);

        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        assertNull(captura.getAllValues().get(1).getClaims().getClaim("dev"));
    }

    @Test
    void renovarConsultaOVinculoComOAparelhoDoTokenEMantemEleNoNovoToken() {
        Jwt.Builder b = Jwt.withTokenValue("r").header("alg", "RS256").claim("token_type", "refresh")
                .subject("Vanderson Felix").jti("jti-9").claim("login", "vanderson").claim("dev", "hash-do-celular")
                .expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now().minusSeconds(10));
        when(jwtDecoder.decode("refresh-antigo")).thenReturn(b.build());
        when(redis.hasKey("auth:revoked:jti-9")).thenReturn(false);
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);

        service.refresh("refresh-antigo");

        verify(vinculoDispositivo).validarNoRefresh(usuario, "hash-do-celular");
        ArgumentCaptor<JwtEncoderParameters> captura = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captura.capture());
        assertEquals("hash-do-celular", captura.getAllValues().get(1).getClaims().getClaimAsString("dev"));
    }

    @Test
    void refreshDeAparelhoQueNaoEOVinculadoEhRecusadoSemEmitirNada() {
        Jwt.Builder b = Jwt.withTokenValue("r").header("alg", "RS256").claim("token_type", "refresh")
                .subject("Vanderson Felix").jti("jti-9").claim("login", "vanderson").claim("dev", "outro")
                .expiresAt(Instant.now().plusSeconds(3600)).issuedAt(Instant.now().minusSeconds(10));
        when(jwtDecoder.decode("refresh-antigo")).thenReturn(b.build());
        when(redis.hasKey("auth:revoked:jti-9")).thenReturn(false);
        when(buscarUsuarioService.buscarUsuarioPorlogin("vanderson")).thenReturn(usuario);
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão encerrada."))
                .when(vinculoDispositivo).validarNoRefresh(usuario, "outro");

        negado(() -> service.refresh("refresh-antigo"));

        verify(jwtEncoder, never()).encode(any());
    }
}
