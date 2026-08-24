package br.com.frotasPro.api.service.auditoria;

import br.com.frotasPro.api.config.ApplicationContextProvider;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.util.AuditoriaEntidadeResolver;
import br.com.frotasPro.api.util.AuditoriaSnapshotSerializer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Chamado pelos callbacks de ciclo de vida do JPA em {@code AuditoriaBase}
 * (@PostPersist/@PreUpdate/@PreRemove) — grava a auditoria com o antes/depois
 * de verdade da entidade, direto do objeto que o Hibernate está persistindo.
 * Roda de dentro da própria entidade (não é um bean Spring), por isso pega o
 * service via {@link ApplicationContextProvider} em vez de injeção normal.
 */
public class AuditoriaEntidadeRegistrador {

    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaEntidadeRegistrador.class);

    private AuditoriaEntidadeRegistrador() {
    }

    public static void registrarCriacao(Object entidade) {
        registrar(entidade, AcaoAuditoria.CRIACAO, null, AuditoriaSnapshotSerializer.serializar(entidade));
    }

    public static void registrarAtualizacao(Object entidade, String snapshotAnterior) {
        String atual = AuditoriaSnapshotSerializer.serializar(entidade);
        if (snapshotAnterior != null && snapshotAnterior.equals(atual)) {
            return; // nenhum campo simples mudou (ex: só uma associação lazy foi tocada)
        }
        registrar(entidade, AcaoAuditoria.ATUALIZACAO, snapshotAnterior, atual);
    }

    public static void registrarExclusao(Object entidade) {
        registrar(entidade, AcaoAuditoria.EXCLUSAO, AuditoriaSnapshotSerializer.serializar(entidade), null);
    }

    private static void registrar(Object entidade, AcaoAuditoria acao, String dadosAntes, String dadosDepois) {
        try {
            RegistrarLogAuditoriaService service = ApplicationContextProvider.getBean(RegistrarLogAuditoriaService.class);
            if (service == null) {
                return; // contexto Spring ainda não subiu (ex: durante inicialização/testes)
            }

            String entidadeLabel = AuditoriaEntidadeResolver.resolverLabel(entidade.getClass());
            String identificador = AuditoriaEntidadeResolver.resolverIdentificador(entidade);
            String descricao = descrever(acao, entidadeLabel, identificador);

            String usuarioLogin = null;
            String usuarioNome = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
                usuarioLogin = jwt.getClaimAsString("login");
                usuarioNome = jwt.getSubject();
            }

            String metodoHttp = null;
            String endpoint = null;
            String ip = null;
            try {
                if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                    HttpServletRequest request = attrs.getRequest();
                    metodoHttp = request.getMethod();
                    endpoint = request.getRequestURI();
                    ip = extrairIp(request);
                }
            } catch (Exception ignored) {
                // fora de um request HTTP (ex: job agendado) — segue sem esses dados
            }

            service.registrar(usuarioLogin, usuarioNome, acao, entidadeLabel, descricao, metodoHttp, endpoint, null, ip, dadosAntes, dadosDepois);
            AuditoriaEntidadeContexto.marcarAuditado();
        } catch (Exception e) {
            LOG.error("Falha ao auditar entidade {}: {}", entidade.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String descrever(AcaoAuditoria acao, String entidadeLabel, String identificador) {
        String sufixo = (identificador != null && !identificador.isBlank()) ? " " + identificador : "";
        return switch (acao) {
            case CRIACAO -> "Criou " + entidadeLabel + sufixo;
            case ATUALIZACAO -> "Atualizou " + entidadeLabel + sufixo;
            case EXCLUSAO -> "Excluiu " + entidadeLabel + sufixo;
            default -> entidadeLabel;
        };
    }

    private static String extrairIp(HttpServletRequest request) {
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
