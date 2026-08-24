package br.com.frotasPro.api.service.auditoria;

/**
 * Sinaliza, dentro da mesma requisição/thread, que pelo menos uma entidade
 * já foi auditada em detalhe (com antes/depois) por {@link AuditoriaEntidadeRegistrador}.
 * O {@code LogAuditoriaFilter} usa isso pra não duplicar com o registro
 * genérico dele quando a auditoria de entidade já cobriu a ação.
 */
public class AuditoriaEntidadeContexto {

    private static final ThreadLocal<Boolean> AUDITOU = new ThreadLocal<>();

    private AuditoriaEntidadeContexto() {
    }

    public static void marcarAuditado() {
        AUDITOU.set(Boolean.TRUE);
    }

    public static boolean foiAuditado() {
        return Boolean.TRUE.equals(AUDITOU.get());
    }

    public static void limpar() {
        AUDITOU.remove();
    }
}
