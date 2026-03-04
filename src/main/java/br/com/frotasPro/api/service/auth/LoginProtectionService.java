package br.com.frotasPro.api.service.auth;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginProtectionService {

    private static final String KEY_RATE_IP = "auth:rate:ip:";
    private static final String KEY_RATE_LOGIN = "auth:rate:login:";
    private static final String KEY_FAIL_IP = "auth:fail:ip:";
    private static final String KEY_FAIL_LOGIN = "auth:fail:login:";
    private static final String KEY_BLOCK_IP = "auth:block:ip:";
    private static final String KEY_BLOCK_LOGIN = "auth:block:login:";

    private final StringRedisTemplate redis;
    private final MeterRegistry meterRegistry;

    @Value("${frotapro.security.login.rate-limit.ip.max-attempts:30}")
    private long rateLimitIpMaxAttempts;

    @Value("${frotapro.security.login.rate-limit.ip.window-seconds:60}")
    private long rateLimitIpWindowSeconds;

    @Value("${frotapro.security.login.rate-limit.user.max-attempts:12}")
    private long rateLimitUserMaxAttempts;

    @Value("${frotapro.security.login.rate-limit.user.window-seconds:60}")
    private long rateLimitUserWindowSeconds;

    @Value("${frotapro.security.login.block.first-threshold:5}")
    private long firstThreshold;

    @Value("${frotapro.security.login.block.first-seconds:300}")
    private long firstBlockSeconds;

    @Value("${frotapro.security.login.block.second-threshold:8}")
    private long secondThreshold;

    @Value("${frotapro.security.login.block.second-seconds:900}")
    private long secondBlockSeconds;

    @Value("${frotapro.security.login.block.third-threshold:10}")
    private long thirdThreshold;

    @Value("${frotapro.security.login.block.third-seconds:1800}")
    private long thirdBlockSeconds;

    public void assertAllowed(String ip, String login) {
        assertRateLimit(ip, login);
        assertNotBlocked(ip, login);
    }

    public void registerFailure(String ip, String login) {
        long loginFailures = incrementWithWindow(KEY_FAIL_LOGIN + login, 3600);
        long ipFailures = incrementWithWindow(KEY_FAIL_IP + ip, 3600);

        long loginBlockSeconds = computeBlockSeconds(loginFailures);
        long ipBlockSeconds = computeBlockSeconds(ipFailures);

        if (loginBlockSeconds > 0) {
            redis.opsForValue().set(KEY_BLOCK_LOGIN + login, "1", Duration.ofSeconds(loginBlockSeconds));
        }
        if (ipBlockSeconds > 0) {
            redis.opsForValue().set(KEY_BLOCK_IP + ip, "1", Duration.ofSeconds(ipBlockSeconds));
        }

        if (loginBlockSeconds > 0 || ipBlockSeconds > 0) {
            Counter.builder("security_login_blocked_total")
                    .description("Quantidade de bloqueios progressivos de login")
                    .register(meterRegistry)
                    .increment();
        }
    }

    public void registerSuccess(String ip, String login) {
        redis.delete(KEY_FAIL_LOGIN + login);
        redis.delete(KEY_FAIL_IP + ip);
        redis.delete(KEY_BLOCK_LOGIN + login);
        redis.delete(KEY_BLOCK_IP + ip);

        Counter.builder("security_login_success_total")
                .description("Quantidade de logins com sucesso")
                .register(meterRegistry)
                .increment();
    }

    private void assertRateLimit(String ip, String login) {
        long ipAttempts = incrementWithWindow(KEY_RATE_IP + ip, rateLimitIpWindowSeconds);
        if (ipAttempts > rateLimitIpMaxAttempts) {
            Counter.builder("security_login_rate_limited_total")
                    .tag("dimension", "ip")
                    .description("Quantidade de tentativas de login bloqueadas por rate limit")
                    .register(meterRegistry)
                    .increment();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Tente novamente em instantes.");
        }

        long loginAttempts = incrementWithWindow(KEY_RATE_LOGIN + login, rateLimitUserWindowSeconds);
        if (loginAttempts > rateLimitUserMaxAttempts) {
            Counter.builder("security_login_rate_limited_total")
                    .tag("dimension", "user")
                    .description("Quantidade de tentativas de login bloqueadas por rate limit")
                    .register(meterRegistry)
                    .increment();
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Tente novamente em instantes.");
        }
    }

    private void assertNotBlocked(String ip, String login) {
        boolean loginBlocked = Boolean.TRUE.equals(redis.hasKey(KEY_BLOCK_LOGIN + login));
        boolean ipBlocked = Boolean.TRUE.equals(redis.hasKey(KEY_BLOCK_IP + ip));
        if (loginBlocked || ipBlocked) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Usuário temporariamente bloqueado por tentativas inválidas.");
        }
    }

    private long incrementWithWindow(String key, long windowSeconds) {
        Long current = redis.opsForValue().increment(key);
        if (current != null && current == 1L) {
            redis.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return current == null ? 0L : current;
    }

    private long computeBlockSeconds(long failures) {
        if (failures >= thirdThreshold) {
            return thirdBlockSeconds;
        }
        if (failures >= secondThreshold) {
            return secondBlockSeconds;
        }
        if (failures >= firstThreshold) {
            return firstBlockSeconds;
        }
        return 0L;
    }
}
