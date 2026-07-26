package br.com.frotasPro.api.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CargaMapperTest {

    @Test
    void integracaoAtivaComExternoPreenchido_devePreferirExterno() {
        String resultado = CargaMapper.resolverNumeroExibicao("INT-1", "EXT-1", true);
        assertEquals("EXT-1", resultado);
    }

    @Test
    void integracaoAtivaComExternoNulo_deveUsarInterno() {
        String resultado = CargaMapper.resolverNumeroExibicao("INT-1", null, true);
        assertEquals("INT-1", resultado);
    }

    @Test
    void integracaoAtivaComExternoEmBranco_deveUsarInterno() {
        String resultado = CargaMapper.resolverNumeroExibicao("INT-1", "   ", true);
        assertEquals("INT-1", resultado);
    }

    @Test
    void integracaoInativaComExternoPreenchido_deveUsarInterno() {
        String resultado = CargaMapper.resolverNumeroExibicao("INT-1", "EXT-1", false);
        assertEquals("INT-1", resultado);
    }
}
