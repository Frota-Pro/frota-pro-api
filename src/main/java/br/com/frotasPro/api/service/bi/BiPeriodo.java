package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.excption.BusinessException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Período dos painéis do gestor: "últimos N meses" (como o filtro do Metabase) ou um intervalo
 * escolhido de/até. No modo "últimos N meses" a consulta não tem data final (igual ao Metabase),
 * então lançamentos com data futura continuam entrando.
 */
public record BiPeriodo(Integer meses, LocalDate inicio, LocalDate fim, boolean personalizado) {

    public static final int MESES_PADRAO = 6;
    public static final int MESES_MAXIMO = 24;
    private static final LocalDate SEM_LIMITE = LocalDate.of(2999, 12, 31);

    /** Resolve os parâmetros da requisição. De/até tem prioridade sobre "meses". */
    public static BiPeriodo resolver(Integer meses, LocalDate de, LocalDate ate, LocalDate hoje) {
        if (de != null || ate != null) {
            if (de == null) {
                throw new BusinessException("Informe a data inicial do período.");
            }
            LocalDate fim = ate != null ? ate : hoje;
            if (fim.isBefore(de)) {
                throw new BusinessException("A data final não pode ser anterior à data inicial.");
            }
            if (ChronoUnit.DAYS.between(de, fim) > 31L * MESES_MAXIMO) {
                throw new BusinessException("O período deve ter no máximo " + MESES_MAXIMO + " meses.");
            }
            return new BiPeriodo(null, de, fim, true);
        }

        int n = meses != null ? meses : MESES_PADRAO;
        if (n < 1 || n > MESES_MAXIMO) {
            throw new BusinessException("O período deve ser de 1 a " + MESES_MAXIMO + " meses.");
        }
        return new BiPeriodo(n, hoje.minusMonths(n), hoje, false);
    }

    /** Data final usada nas consultas: sem limite quando o período é "últimos N meses". */
    public LocalDate fimDaConsulta() {
        return personalizado ? fim : SEM_LIMITE;
    }
}
