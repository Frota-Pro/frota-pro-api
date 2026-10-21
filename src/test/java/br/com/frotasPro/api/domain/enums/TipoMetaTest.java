package br.com.frotasPro.api.domain.enums;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoMetaTest {

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    @Test
    void todosOsTiposBatemQuandoORealizadoEMaiorOuIgualAMeta() {
        for (TipoMeta tipo : TipoMeta.values()) {
            assertTrue(tipo.metaAtingida(n("100"), n("100")), tipo + " no limite");
            assertTrue(tipo.metaAtingida(n("120"), n("100")), tipo + " acima");
            assertFalse(tipo.metaAtingida(n("99.9"), n("100")), tipo + " abaixo");
        }
    }

    @Test
    void consumoEmKmPorLitroBatendoMaisEMelhor() {
        // meta: pelo menos 2,96 km/L
        assertTrue(TipoMeta.CONSUMO_COMBUSTIVEL.metaAtingida(n("3.38"), n("2.96")));
        assertTrue(TipoMeta.CONSUMO_COMBUSTIVEL.metaAtingida(n("2.96"), n("2.96")));
        assertFalse(TipoMeta.CONSUMO_COMBUSTIVEL.metaAtingida(n("2.50"), n("2.96")));
    }

    @Test
    void regraDeAtingimentoEMaiorOuIgualParaTodos() {
        for (TipoMeta tipo : TipoMeta.values()) {
            assertEquals("MAIOR_OU_IGUAL", tipo.getRegraAtingimento(), tipo.name());
        }
    }

    @Test
    void semRealizadoOuSemMetaValidaNaoBate() {
        for (TipoMeta tipo : TipoMeta.values()) {
            assertFalse(tipo.metaAtingida(null, n("100")));
            assertFalse(tipo.metaAtingida(n("100"), null));
            assertFalse(tipo.metaAtingida(n("100"), BigDecimal.ZERO));
            assertFalse(tipo.metaAtingida(n("100"), n("-1")));
        }
    }
}
