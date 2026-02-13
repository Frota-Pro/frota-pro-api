package br.com.frotasPro.api.utils;

import br.com.frotasPro.api.excption.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PeriodoValidator {

    private PeriodoValidator() {
    }

    public static void obrigatorio(LocalDate inicio, LocalDate fim, String campo) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe as datas de início e fim" + sufixoCampo(campo) + ".");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido" + sufixoCampo(campo) + ": início não pode ser maior que fim.");
        }
    }

    public static void opcional(LocalDate inicio, LocalDate fim, String campo) {
        if (inicio == null && fim == null) return;
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe início e fim" + sufixoCampo(campo) + " (ou deixe ambos vazios).");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido" + sufixoCampo(campo) + ": início não pode ser maior que fim.");
        }
    }

    public static void obrigatorio(LocalDateTime inicio, LocalDateTime fim, String campo) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe as datas de início e fim" + sufixoCampo(campo) + ".");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido" + sufixoCampo(campo) + ": início não pode ser maior que fim.");
        }
    }

    public static void opcional(LocalDateTime inicio, LocalDateTime fim, String campo) {
        if (inicio == null && fim == null) return;
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe início e fim" + sufixoCampo(campo) + " (ou deixe ambos vazios).");
        }
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido" + sufixoCampo(campo) + ": início não pode ser maior que fim.");
        }
    }

    private static String sufixoCampo(String campo) {
        return (campo == null || campo.isBlank()) ? "" : " (" + campo.trim() + ")";
    }
}
