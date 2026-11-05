package br.com.frotasPro.api.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CargaScheduledReforcoTest {

    @Test
    void nosPrimeirosDiasDoMesVarreTambemOMesAnterior() {
        assertEquals(LocalDate.of(2026, 10, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 1), 7));
        assertEquals(LocalDate.of(2026, 10, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 5), 7));
        assertEquals(LocalDate.of(2026, 10, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 7), 7));
    }

    @Test
    void depoisDosPrimeirosDiasVoltaAoMesCorrente() {
        assertEquals(LocalDate.of(2026, 11, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 8), 7));
        assertEquals(LocalDate.of(2026, 11, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 30), 7));
    }

    @Test
    void viradaDeAnoVoltaParaDezembro() {
        assertEquals(LocalDate.of(2026, 12, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2027, 1, 3), 7));
    }

    @Test
    void zeroDesligaOReforcoDoMesAnterior() {
        assertEquals(LocalDate.of(2026, 11, 1), CargaScheduled.inicioDoReforco(LocalDate.of(2026, 11, 2), 0));
    }
}
