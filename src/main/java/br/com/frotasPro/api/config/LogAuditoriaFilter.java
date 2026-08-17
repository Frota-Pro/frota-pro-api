package br.com.frotasPro.api.config;

import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.service.auditoria.AuditoriaEntidadeContexto;
import br.com.frotasPro.api.service.auditoria.RegistrarLogAuditoriaService;
import br.com.frotasPro.api.util.AuditoriaDescricaoResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Rede de segurança da trilha de auditoria: cobre qualquer requisição que
 * altera dados (POST/PUT/PATCH/DELETE) cuja entidade NÃO passou pela
 * auditoria detalhada de {@code AuditoriaBase} (ex: entidades que ainda não
 * estendem ela) — sem isso, uma entidade fora desse esquema ficaria sem
 * nenhum registro. Quando a auditoria de entidade já rodou pra essa
 * requisição (sinalizado via {@link AuditoriaEntidadeContexto}), esse filtro
 * não faz nada, pra não duplicar. Login/logout são registrados à parte (em
 * {@code UsuarioLoginService}/{@code AuthTokenService}) porque acontecem
 * antes de existir um JWT autenticado nessa requisição.
 */
@Component
public class LogAuditoriaFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_AUDITADOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final Set<String> CAMINHOS_IGNORADOS = Set.of(
            "/login", "/auth/login",
            "/login/refresh", "/auth/login/refresh",
            "/login/logout", "/auth/login/logout",
            "/usuario/me/dispositivo-app"
    );

    private final RegistrarLogAuditoriaService registrarService;

    public LogAuditoriaFilter(RegistrarLogAuditoriaService registrarService) {
        this.registrarService = registrarService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
            registrarSeNecessario(request, response);
        } finally {
            AuditoriaEntidadeContexto.limpar();
        }
    }

    private void registrarSeNecessario(HttpServletRequest request, HttpServletResponse response) {
        if (!deveAuditar(request) || AuditoriaEntidadeContexto.foiAuditado()) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication != null && authentication.getCredentials() instanceof Jwt jwt)) {
            // requisição não autenticada (ex: token inválido) — nada pra atribuir a ninguém
            return;
        }

        String path = request.getRequestURI();
        String metodo = request.getMethod();
        AcaoAuditoria acao = AuditoriaDescricaoResolver.resolverAcao(metodo);
        if (acao == null) {
            return;
        }

        String entidade = AuditoriaDescricaoResolver.resolverEntidade(path);
        String descricao = AuditoriaDescricaoResolver.resolverDescricao(metodo, path, acao);

        registrarService.registrar(
                jwt.getClaimAsString("login"),
                jwt.getSubject(),
                acao,
                entidade,
                descricao,
                metodo,
                path,
                response.getStatus(),
                extractClientIp(request)
        );
    }

    private boolean deveAuditar(HttpServletRequest request) {
        if (!METODOS_AUDITADOS.contains(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        if (CAMINHOS_IGNORADOS.contains(path)) {
            return false;
        }
        return !path.startsWith("/actuator");
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }
}
