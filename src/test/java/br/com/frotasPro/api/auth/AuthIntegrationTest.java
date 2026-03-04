package br.com.frotasPro.api.auth;

import br.com.frotasPro.api.controller.LoginController;
import br.com.frotasPro.api.controller.AuthController;
import br.com.frotasPro.api.controller.request.LoginRequest;
import br.com.frotasPro.api.controller.response.LoginResponse;
import br.com.frotasPro.api.domain.Usuario;
import br.com.frotasPro.api.repository.UsuarioRepository;
import br.com.frotasPro.api.service.auth.AuthTokenService;
import br.com.frotasPro.api.service.usuario.UsuarioLoginService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ResponseStatusException;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LoginController.class, AuthController.class})
@Import({AuthIntegrationTest.TestSecurityConfig.class})
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @org.springframework.boot.test.mock.mockito.MockBean
    private UsuarioLoginService usuarioLoginService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuthTokenService authTokenService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    void loginValidoDeveRetornar200EParDeTokens() throws Exception {
        when(usuarioLoginService.login(ArgumentMatchers.any(LoginRequest.class), ArgumentMatchers.anyString()))
                .thenReturn(new LoginResponse("access", 900L, "refresh", 604800L));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"admin","senha":"padrao123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshExpiresIn").value(604800));
    }

    @Test
    void loginInvalidoDeveRetornar401() throws Exception {
        when(usuarioLoginService.login(ArgumentMatchers.any(LoginRequest.class), ArgumentMatchers.anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"admin","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaComTokenValidoDeveRetornar200() throws Exception {
        String token = generateToken(Instant.now().plusSeconds(300));
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        when(usuarioRepository.findByNome("Administrador")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"));
    }

    @Test
    void rotaProtegidaComTokenExpiradoDeveRetornar401() throws Exception {
        String token = generateToken(Instant.now().minusSeconds(60));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaComTokenAssinaturaInvalidaDeveRetornar401() throws Exception {
        String invalidSignatureToken = generateTokenWithAnotherKey();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + invalidSignatureToken))
                .andExpect(status().isUnauthorized());
    }

    private String generateToken(Instant expiresAt) {
        Instant issuedAt = expiresAt.minusSeconds(60);
        org.springframework.security.oauth2.jwt.JwtClaimsSet claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("frotaPro-api")
                .subject("Administrador")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("token_type", "access")
                .claim("login", "admin")
                .claim("id", UUID.randomUUID().toString())
                .claim("scope", java.util.List.of("ROLE_ADMIN"))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateTokenWithAnotherKey() throws JOSEException {
        RSAKey rsaJwk = new RSAKeyGenerator(2048).generate();
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new JWTClaimsSet.Builder()
                        .issuer("frotaPro-api")
                        .subject("Administrador")
                        .issueTime(java.util.Date.from(Instant.now()))
                        .expirationTime(java.util.Date.from(Instant.now().plusSeconds(300)))
                        .jwtID(UUID.randomUUID().toString())
                        .claim("token_type", "access")
                        .claim("login", "admin")
                        .claim("id", UUID.randomUUID().toString())
                        .claim("scope", java.util.List.of("ROLE_ADMIN"))
                        .build()
        );

        signedJWT.sign(new RSASSASigner(rsaJwk.toPrivateKey()));
        return signedJWT.serialize();
    }

    @TestConfiguration
    static class TestSecurityConfig {

        private final RSAKey keyPair;

        TestSecurityConfig() throws JOSEException {
            this.keyPair = new RSAKeyGenerator(2048).generate();
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/login").permitAll()
                            .anyRequest().authenticated()
                    )
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                    .build();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
            JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
            converter.setAuthorityPrefix("");
            converter.setAuthoritiesClaimName("scope");
            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(converter);
            return jwtAuthenticationConverter;
        }

        @Bean
        JwtDecoder jwtDecoder() {
            try {
                return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.toPublicKey()).build();
            } catch (JOSEException e) {
                throw new IllegalStateException("Falha ao criar JwtDecoder de teste", e);
            }
        }

        @Bean
        JwtEncoder jwtEncoder() {
            try {
                JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.toPublicKey())
                        .privateKey((RSAPrivateKey) keyPair.toPrivateKey())
                        .build();
                return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
            } catch (JOSEException e) {
                throw new IllegalStateException("Falha ao criar JwtEncoder de teste", e);
            }
        }
    }
}
