package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.excption.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiPeriodoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 23);

    @Test
    void semParametrosUsaOsUltimosSeisMeses() {
        BiPeriodo p = BiPeriodo.resolver(null, null, null, HOJE);

        assertEquals(Integer.valueOf(6), p.meses());
        assertEquals(LocalDate.of(2026, 4, 23), p.inicio());
        assertEquals(HOJE, p.fim());
        assertFalse(p.personalizado());
    }

    @Test
    void ultimosMesesNaoLimitaADataFinalDaConsulta() {
        BiPeriodo p = BiPeriodo.resolver(3, null, null, HOJE);

        assertTrue(p.fimDaConsulta().isAfter(HOJE.plusYears(100)));
        assertEquals(HOJE, p.fim());
    }

    @Test
    void mesesForaDoLimiteDaErro() {
        assertThrows(BusinessException.class, () -> BiPeriodo.resolver(0, null, null, HOJE));
        assertThrows(BusinessException.class, () -> BiPeriodo.resolver(25, null, null, HOJE));
        assertEquals(Integer.valueOf(24), BiPeriodo.resolver(24, null, null, HOJE).meses());
        assertEquals(Integer.valueOf(1), BiPeriodo.resolver(1, null, null, HOJE).meses());
    }

    @Test
    void intervaloEscolhidoGanhaDosMeses() {
        BiPeriodo p = BiPeriodo.resolver(12, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), HOJE);

        assertTrue(p.personalizado());
        assertNull(p.meses());
        assertEquals(LocalDate.of(2026, 10, 1), p.inicio());
        assertEquals(LocalDate.of(2026, 10, 15), p.fim());
        assertEquals(LocalDate.of(2026, 10, 15), p.fimDaConsulta());
    }

    @Test
    void semDataFinalVaiAteHoje() {
        BiPeriodo p = BiPeriodo.resolver(null, LocalDate.of(2026, 10, 1), null, HOJE);

        assertEquals(HOJE, p.fim());
    }

    @Test
    void soDataFinalSemInicialDaErro() {
        assertThrows(BusinessException.class, () -> BiPeriodo.resolver(null, null, LocalDate.of(2026, 10, 15), HOJE));
    }

    @Test
    void dataFinalAntesDaInicialDaErro() {
        assertThrows(BusinessException.class, () -> BiPeriodo.resolver(null, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), HOJE));
    }

    @Test
    void intervaloMuitoLongoDaErro() {
        assertThrows(BusinessException.class, () -> BiPeriodo.resolver(null, LocalDate.of(2020, 1, 1), HOJE, HOJE));
    }

    @Test
    void umUnicoDiaEValido() {
        BiPeriodo p = BiPeriodo.resolver(null, HOJE, HOJE, HOJE);

        assertEquals(HOJE, p.inicio());
        assertEquals(HOJE, p.fim());
    }
}
