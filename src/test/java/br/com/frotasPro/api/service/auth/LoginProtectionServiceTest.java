package br.com.frotasPro.api.service.auth;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginProtectionServiceTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> ops;

    private SimpleMeterRegistry metricas;
    private LoginProtectionService service;

    @BeforeEach
    void setUp() {
        metricas = new SimpleMeterRegistry();
        service = new LoginProtectionService(redis, metricas);
        // os @Value nao sao aplicados fora do Spring: usa os mesmos padroes da aplicacao
        ReflectionTestUtils.setField(service, "rateLimitIpMaxAttempts", 30L);
        ReflectionTestUtils.setField(service, "rateLimitIpWindowSeconds", 60L);
        ReflectionTestUtils.setField(service, "rateLimitUserMaxAttempts", 12L);
        ReflectionTestUtils.setField(service, "rateLimitUserWindowSeconds", 60L);
        ReflectionTestUtils.setField(service, "firstThreshold", 5L);
        ReflectionTestUtils.setField(service, "firstBlockSeconds", 300L);
        ReflectionTestUtils.setField(service, "secondThreshold", 8L);
        ReflectionTestUtils.setField(service, "secondBlockSeconds", 900L);
        ReflectionTestUtils.setField(service, "thirdThreshold", 10L);
        ReflectionTestUtils.setField(service, "thirdBlockSeconds", 1800L);

        lenient().when(redis.opsForValue()).thenReturn(ops);
    }

    private double contador(String nome) {
        var c = metricas.find(nome).counter();
        return c == null ? 0 : c.count();
    }

    private void tentativas(String chave, long total) {
        when(ops.increment(chave)).thenReturn(total);
    }

    // ---- limite de tentativas (rate limit)

    @Test
    void dentroDosLimitesELiberado() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:ze", 1L);

        assertDoesNotThrow(() -> service.assertAllowed("1.1.1.1", "ze"));
    }

    @Test
    void primeiraTentativaDaJanelaDefineAValidadeDoContador() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:ze", 1L);

        service.assertAllowed("1.1.1.1", "ze");

        verify(redis).expire("auth:rate:ip:1.1.1.1", Duration.ofSeconds(60));
        verify(redis).expire("auth:rate:login:ze", Duration.ofSeconds(60));
    }

    @Test
    void tentativasSeguintesNaoRenovamAValidade() {
        tentativas("auth:rate:ip:1.1.1.1", 2L);
        tentativas("auth:rate:login:ze", 2L);

        service.assertAllowed("1.1.1.1", "ze");

        verify(redis, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void passarDe30TentativasPorIpDa429() {
        tentativas("auth:rate:ip:1.1.1.1", 31L);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.assertAllowed("1.1.1.1", "ze"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
        assertEquals(1.0, contador("security_login_rate_limited_total"));
    }

    @Test
    void exatamenteNoLimiteDeIpAindaPassa() {
        tentativas("auth:rate:ip:1.1.1.1", 30L);
        tentativas("auth:rate:login:ze", 1L);

        assertDoesNotThrow(() -> service.assertAllowed("1.1.1.1", "ze"));
    }

    @Test
    void passarDe12TentativasPorUsuarioDa429() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:ze", 13L);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.assertAllowed("1.1.1.1", "ze"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
        assertEquals(1.0, metricas.find("security_login_rate_limited_total").tag("dimension", "user").counter().count());
    }

    // ---- bloqueio progressivo

    @Test
    void loginBloqueadoNaoEntra() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:ze", 1L);
        when(redis.hasKey("auth:block:login:ze")).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> service.assertAllowed("1.1.1.1", "ze"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, e.getStatusCode());
        assertTrue(e.getReason().contains("temporariamente bloqueado"));
    }

    @Test
    void ipBloqueadoNaoEntraMesmoComOutroUsuario() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:outro", 1L);
        when(redis.hasKey("auth:block:login:outro")).thenReturn(false);
        when(redis.hasKey("auth:block:ip:1.1.1.1")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.assertAllowed("1.1.1.1", "outro"));
    }

    @Test
    void semBloqueioNemLimiteEntra() {
        tentativas("auth:rate:ip:1.1.1.1", 1L);
        tentativas("auth:rate:login:ze", 1L);
        when(redis.hasKey(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> service.assertAllowed("1.1.1.1", "ze"));
    }

    // ---- falhas de login

    private void falhas(long doLogin, long doIp) {
        when(ops.increment("auth:fail:login:ze")).thenReturn(doLogin);
        when(ops.increment("auth:fail:ip:1.1.1.1")).thenReturn(doIp);
    }

    @Test
    void poucasFalhasSoContamEAindaNaoBloqueiam() {
        falhas(4L, 4L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops, never()).set(anyString(), anyString(), any(Duration.class));
        assertEquals(1.0, contador("security_login_failed_total"));
        assertEquals(0.0, contador("security_login_blocked_total"));
    }

    @Test
    void quintaFalhaBloqueiaPorCincoMinutos() {
        falhas(5L, 1L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops).set("auth:block:login:ze", "1", Duration.ofSeconds(300));
        verify(ops, never()).set(org.mockito.ArgumentMatchers.eq("auth:block:ip:1.1.1.1"), anyString(), any(Duration.class));
        assertEquals(1.0, contador("security_login_blocked_total"));
    }

    @Test
    void oitavaFalhaBloqueiaPorQuinzeMinutos() {
        falhas(8L, 1L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops).set("auth:block:login:ze", "1", Duration.ofSeconds(900));
    }

    @Test
    void decimaFalhaBloqueiaPorMeiaHora() {
        falhas(10L, 1L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops).set("auth:block:login:ze", "1", Duration.ofSeconds(1800));
    }

    @Test
    void depoisDaDecimaFalhaContinuaNoBloqueioMaximo() {
        falhas(25L, 1L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops).set("auth:block:login:ze", "1", Duration.ofSeconds(1800));
    }

    @Test
    void muitasFalhasPeloMesmoIpBloqueiamOIpEnaoOUsuario() {
        falhas(1L, 5L);

        service.registerFailure("1.1.1.1", "ze");

        verify(ops).set("auth:block:ip:1.1.1.1", "1", Duration.ofSeconds(300));
        verify(ops, never()).set(org.mockito.ArgumentMatchers.eq("auth:block:login:ze"), anyString(), any(Duration.class));
    }

    @Test
    void contadorDeFalhasComecaComValidadeDeUmaHora() {
        falhas(1L, 1L);

        service.registerFailure("1.1.1.1", "ze");

        verify(redis).expire("auth:fail:login:ze", Duration.ofSeconds(3600));
        verify(redis).expire("auth:fail:ip:1.1.1.1", Duration.ofSeconds(3600));
    }

    // ---- sucesso

    @Test
    void loginComSucessoZeraFalhasEBloqueios() {
        service.registerSuccess("1.1.1.1", "ze");

        verify(redis).delete("auth:fail:login:ze");
        verify(redis).delete("auth:fail:ip:1.1.1.1");
        verify(redis).delete("auth:block:login:ze");
        verify(redis).delete("auth:block:ip:1.1.1.1");
        assertEquals(1.0, contador("security_login_success_total"));
    }
}
