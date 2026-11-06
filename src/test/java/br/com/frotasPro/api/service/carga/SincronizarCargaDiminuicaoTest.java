package br.com.frotasPro.api.service.carga;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SincronizarCargaDiminuicaoTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void pesoDoWinThorComMaisCasasQueOGuardadoNaoEhDiminuicao() {
        // WinThor manda 14,5896 t; o banco guardou 14,590 (arredondado para cima).
        assertFalse(SincronizarCargaService.isDiminuicao(bd("14.590"), bd("14.5896")));
        // Caso real do histórico de 06/11: 0,053 guardado, WinThor com mais casas.
        assertFalse(SincronizarCargaService.isDiminuicao(bd("0.053"), bd("0.05274")));
    }

    @Test
    void diminuicaoDeVerdadeContinuaSendoDiminuicao() {
        assertTrue(SincronizarCargaService.isDiminuicao(bd("14.590"), bd("14.589")));
        assertTrue(SincronizarCargaService.isDiminuicao(bd("406.561"), bd("0.54656")));
        assertTrue(SincronizarCargaService.isDiminuicao(bd("6805.18"), bd("6805.17")));
    }

    @Test
    void aumentoEIgualNuncaSaoDiminuicao() {
        assertFalse(SincronizarCargaService.isDiminuicao(bd("1.000"), bd("1.0004")));
        assertFalse(SincronizarCargaService.isDiminuicao(bd("1.000"), bd("2.000")));
        assertFalse(SincronizarCargaService.isDiminuicao(bd("1.000"), bd("1.000")));
    }

    @Test
    void nuloContaComoZero() {
        assertTrue(SincronizarCargaService.isDiminuicao(bd("1.000"), null));
        assertFalse(SincronizarCargaService.isDiminuicao(null, bd("1.000")));
        assertFalse(SincronizarCargaService.isDiminuicao(null, null));
    }
}
