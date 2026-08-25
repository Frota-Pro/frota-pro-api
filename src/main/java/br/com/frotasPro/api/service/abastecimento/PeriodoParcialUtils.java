package br.com.frotasPro.api.service.abastecimento;

import java.time.LocalDateTime;

/**
 * A tela de Abastecimentos deixa preencher só "De" ou só "Até" no filtro de
 * período (não faz sentido obrigar as duas datas quando a intenção é óbvia:
 * "desde tal dia" ou "até tal dia"). {@link br.com.frotasPro.api.utils.PeriodoValidator#opcional}
 * é compartilhado com outras telas que exigem os dois lados juntos, então em
 * vez de mudar o validador, preenchemos o lado que faltou com uma borda bem
 * aberta ANTES de validar — do ponto de vista do validador, os dois lados
 * sempre chegam preenchidos (ou os dois vazios).
 */
final class PeriodoParcialUtils {

    private static final LocalDateTime INICIO_ABERTO = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    private static final LocalDateTime FIM_ABERTO = LocalDateTime.of(2999, 12, 31, 23, 59, 59);

    private PeriodoParcialUtils() {
    }

    record Periodo(LocalDateTime inicio, LocalDateTime fim) {
    }

    static Periodo abrirLadoAusente(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio != null && fim == null) {
            return new Periodo(inicio, FIM_ABERTO);
        }
        if (fim != null && inicio == null) {
            return new Periodo(INICIO_ABERTO, fim);
        }
        return new Periodo(inicio, fim);
    }
}
