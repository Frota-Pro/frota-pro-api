package br.com.frotasPro.api.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Dia útil = segunda a sexta. Feriados não são considerados (não existe cadastro
 * de feriados no sistema) — se um dia isso for preciso, é só trocar {@link #isDiaUtil}.
 */
public final class DiaUtilUtils {

    private DiaUtilUtils() {
    }

    public static boolean isDiaUtil(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
    }

    /**
     * O n-ésimo dia útil DEPOIS de {@code base} (a própria base nunca conta).
     * Ex.: base = 30/09 (qua), n = 5 → 07/10 (qua): 01/10, 02/10, 05/10, 06/10, 07/10.
     * n &lt;= 0 devolve a própria base.
     */
    public static LocalDate enesimoDiaUtilApos(LocalDate base, int n) {
        LocalDate data = base;
        int contados = 0;
        while (contados < n) {
            data = data.plusDays(1);
            if (isDiaUtil(data)) {
                contados++;
            }
        }
        return data;
    }
}
