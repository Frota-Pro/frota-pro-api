package br.com.frotasPro.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Enquanto o usuário estiver com a senha padrão (ou uma senha resetada por um
 * admin), bloqueia qualquer chamada que não seja a de trocar a própria senha
 * (ou as de login/refresh/logout) — o bloqueio é aqui no backend pra não
 * depender só da tela do front/mobile redirecionar certo.
 */
@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final Set<String> CAMINHOS_PERMITIDOS = Set.of(
            "/usuario/me/senha",
            "/auth/me",
            "/login/refresh",
            "/auth/login/refresh",
            "/login/logout",
            "/auth/login/logout"
    );

    private final ObjectMapper objectMapper;

    public MustChangePasswordFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || estaPermitido(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
            Boolean mustChangePassword = jwt.getClaimAsBoolean("mustChangePassword");
            if (Boolean.TRUE.equals(mustChangePassword)) {
                bloquear(response, request.getRequestURI());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean estaPermitido(HttpServletRequest request) {
        return CAMINHOS_PERMITIDOS.contains(request.getRequestURI());
    }

    private void bloquear(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Must-Change-Password", "true");

        Map<String, Object> corpo = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpServletResponse.SC_FORBIDDEN,
                "error", "Você precisa trocar sua senha antes de continuar.",
                "path", path
        );
        response.getWriter().write(objectMapper.writeValueAsString(corpo));
    }
}
