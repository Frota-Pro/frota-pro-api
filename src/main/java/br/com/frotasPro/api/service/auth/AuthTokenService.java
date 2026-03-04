package br.com.frotasPro.api.service.auth;

import br.com.frotasPro.api.domain.Acesso;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.service.usuario.BuscarUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String REVOKED_PREFIX = "auth:revoked:";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final BuscarUsuarioService buscarUsuarioService;

    @Value("${frotapro.security.jwt.access-token-seconds:900}")
    private long accessTokenSeconds;

    @Value("${frotapro.security.jwt.refresh-token-seconds:604800}")
    private long refreshTokenSeconds;

    public TokenPair generateTokenPair(Usuario usuario) {
        List<String> acessos = usuario.getAcesso().stream().map(Acesso::getNome).toList();
        return generateTokenPair(usuario, acessos);
    }

    public TokenPair refresh(String refreshToken) {
        Jwt decoded = decode(refreshToken);
        validateTokenType(decoded, TOKEN_TYPE_REFRESH);
        validateNotRevoked(decoded);

        revoke(decoded);

        String login = decoded.getClaimAsString("login");
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token inválido");
        }

        Usuario usuario = buscarUsuarioService.buscarUsuarioPorlogin(login);
        List<String> acessos = usuario.getAcesso().stream().map(Acesso::getNome).toList();

        return generateTokenPair(usuario, acessos);
    }

    public void logout(String refreshToken) {
        Jwt decoded = decode(refreshToken);
        validateTokenType(decoded, TOKEN_TYPE_REFRESH);
        validateNotRevoked(decoded);
        revoke(decoded);
    }

    private TokenPair generateTokenPair(Usuario usuario, List<String> acessos) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusSeconds(accessTokenSeconds);
        Instant refreshExpiresAt = now.plusSeconds(refreshTokenSeconds);

        String accessJti = UUID.randomUUID().toString();
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .issuer("frotaPro-api")
                .subject(usuario.getNome())
                .issuedAt(now)
                .expiresAt(accessExpiresAt)
                .id(accessJti)
                .claim("token_type", TOKEN_TYPE_ACCESS)
                .claim("login", usuario.getLogin())
                .claim("id", usuario.getId())
                .claim("scope", acessos)
                .build();

        String refreshJti = UUID.randomUUID().toString();
        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .issuer("frotaPro-api")
                .subject(usuario.getNome())
                .issuedAt(now)
                .expiresAt(refreshExpiresAt)
                .id(refreshJti)
                .claim("token_type", TOKEN_TYPE_REFRESH)
                .claim("login", usuario.getLogin())
                .claim("id", usuario.getId())
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();
        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();

        return new TokenPair(accessToken, accessTokenSeconds, refreshToken, refreshTokenSeconds);
    }

    private Jwt decode(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido");
        }
    }

    private void validateTokenType(Jwt token, String expectedType) {
        String tokenType = token.getClaimAsString("token_type");
        if (!expectedType.equals(tokenType)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido para esta operação");
        }
    }

    private void validateNotRevoked(Jwt token) {
        String jti = token.getId();
        if (jti == null || jti.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido");
        }
        Boolean exists = stringRedisTemplate.hasKey(REVOKED_PREFIX + jti);
        if (Boolean.TRUE.equals(exists)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token já revogado");
        }
    }

    private void revoke(Jwt token) {
        String jti = token.getId();
        Instant expiresAt = token.getExpiresAt();
        if (jti == null || jti.isBlank() || expiresAt == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Token inválido");
        }

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        stringRedisTemplate.opsForValue().set(REVOKED_PREFIX + jti, "1", ttl);
    }
}
