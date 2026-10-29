package br.com.frotasPro.api.util;

import br.com.frotasPro.api.service.usuario.SenhaPadraoPolitica;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorSenhaTest {

    @Test
    void geraSenhaDe10CaracteresSemOsQueSeConfundem() {
        for (int i = 0; i < 500; i++) {
            String senha = GeradorSenha.gerar();
            assertEquals(10, senha.length());
            assertTrue(senha.matches("[A-HJ-NP-Za-km-z2-9]{10}"), senha);
        }
    }

    @Test
    void senhasNaoSeRepetem() {
        Set<String> geradas = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            geradas.add(GeradorSenha.gerar());
        }
        assertEquals(2000, geradas.size());
    }

    @Test
    void politicaDaSenhaPadraoSoBloqueiaQuandoLigada() {
        assertFalse(new SenhaPadraoPolitica(false).deveBloquear("padrao123"));
        assertTrue(new SenhaPadraoPolitica(true).deveBloquear("padrao123"));
        assertFalse(new SenhaPadraoPolitica(true).deveBloquear("outraSenha1"));
        assertFalse(new SenhaPadraoPolitica(true).deveBloquear(null));
    }
}
