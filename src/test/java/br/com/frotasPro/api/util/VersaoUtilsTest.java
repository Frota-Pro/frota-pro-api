package br.com.frotasPro.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersaoUtilsTest {

    @Test
    void versoesIguais_retornaZero() {
        assertEquals(0, VersaoUtils.compareVersoes("1.2.3", "1.2.3"));
    }

    @Test
    void primeiraMaisNova_retornaPositivo() {
        assertTrue(VersaoUtils.compareVersoes("1.3.0", "1.2.9") > 0);
    }

    @Test
    void primeiraMaisAntiga_retornaNegativo() {
        assertTrue(VersaoUtils.compareVersoes("1.1.1", "1.2.0") < 0);
    }

    @Test
    void ignoraSufixoDeBuild() {
        assertEquals(0, VersaoUtils.compareVersoes("1.1.1+5", "1.1.1+9"));
    }

    @Test
    void tamanhosDiferentes_completaComZero() {
        assertTrue(VersaoUtils.compareVersoes("1.2", "1.2.1") < 0);
        assertEquals(0, VersaoUtils.compareVersoes("1.2.0", "1.2"));
    }
}
