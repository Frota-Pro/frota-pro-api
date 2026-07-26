package br.com.frotasPro.api.util;

import java.util.Arrays;
import java.util.List;

/**
 * Compara versões no formato "1.2.3". Mesma lógica de
 * frota-pro-mobile/lib/features/app_update/domain/version_comparator.dart —
 * mantenha os dois espelhados para mobile e backend nunca divergirem sobre
 * qual versão é mais nova.
 */
public class VersaoUtils {

    private VersaoUtils() {
    }

    /**
     * Retorna > 0 se {@code a} for mais nova que {@code b}, < 0 se for mais
     * antiga, e 0 se forem iguais.
     */
    public static int compareVersoes(String a, String b) {
        List<Integer> partesA = partesVersao(a);
        List<Integer> partesB = partesVersao(b);
        int tamanho = Math.max(partesA.size(), partesB.size());

        for (int i = 0; i < tamanho; i++) {
            int va = i < partesA.size() ? partesA.get(i) : 0;
            int vb = i < partesB.size() ? partesB.get(i) : 0;
            if (va != vb) {
                return Integer.compare(va, vb);
            }
        }
        return 0;
    }

    private static List<Integer> partesVersao(String versao) {
        String semBuild = versao == null ? "" : versao.split("\\+")[0];
        return Arrays.stream(semBuild.split("\\."))
                .map(VersaoUtils::parseOuZero)
                .toList();
    }

    private static Integer parseOuZero(String parte) {
        try {
            return Integer.parseInt(parte.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
