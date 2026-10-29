package br.com.frotasPro.api.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NomeCidadeUtilsTest {

    private static final List<String> FRIOS = NomeCidadeUtils.sufixos("FRIOS");
    private static final List<String> NENHUM = NomeCidadeUtils.sufixos(null);

    @Test
    void ignoraAcentoCaixaEEspacos() {
        assertEquals("POCO DANTAS", NomeCidadeUtils.chave("POÇO DANTAS", NENHUM));
        assertEquals("POCO DANTAS", NomeCidadeUtils.chave("poco dantas", NENHUM));
        assertEquals("POCO DANTAS", NomeCidadeUtils.chave("  Poço   Dantas ", NENHUM));
        assertEquals("CONCEICAO", NomeCidadeUtils.chave("CONCEIÇÃO", NENHUM));
    }

    @Test
    void sufixoConfiguradoEhAMesmaRota() {
        assertEquals("SOUSA", NomeCidadeUtils.chave("SOUSA FRIOS", FRIOS));
        assertEquals("SOUSA", NomeCidadeUtils.chave("Sousa  frios ", FRIOS));
        assertEquals("VALE 1", NomeCidadeUtils.chave("VALE 1 FRIOS", FRIOS));
        assertEquals("POCO DANTAS", NomeCidadeUtils.chave("POÇO DANTAS FRIOS", FRIOS));
    }

    @Test
    void semSufixoConfiguradoFriosEhOutraRota() {
        assertNotEquals(NomeCidadeUtils.chave("SOUSA", NENHUM), NomeCidadeUtils.chave("SOUSA FRIOS", NENHUM));
    }

    @Test
    void sufixoSoValeNoFimEDepoisDeOutraPalavra() {
        assertEquals("FRIOS", NomeCidadeUtils.chave("FRIOS", FRIOS));
        assertEquals("FRIOSUL", NomeCidadeUtils.chave("FRIOSUL", FRIOS));
        assertEquals("FRIOS SOUSA", NomeCidadeUtils.chave("FRIOS SOUSA", FRIOS));
    }

    @Test
    void variosSufixosEComAcentoOuMaisDeUmaPalavra() {
        List<String> sufixos = NomeCidadeUtils.sufixos(" frios ; Refrigerado , carga seca,, FRIOS");
        assertEquals(List.of("FRIOS", "REFRIGERADO", "CARGA SECA"), sufixos);
        assertEquals("SOUSA", NomeCidadeUtils.chave("Sousa carga seca", sufixos));
        assertEquals("PATOS", NomeCidadeUtils.chave("PATOS REFRIGERADO", sufixos));
        assertEquals("SOUSA", NomeCidadeUtils.chave("SOUSA", sufixos));
    }

    @Test
    void grafiasDiferentesContinuamDiferentes() {
        assertNotEquals(NomeCidadeUtils.chave("POÇO DANTAS", FRIOS), NomeCidadeUtils.chave("POCO DANTA", FRIOS));
    }

    @Test
    void nomeDaRotaTiraOSufixoMasMantemAcento() {
        assertEquals("POÇO DANTAS", NomeCidadeUtils.nomeDaRota("POÇO DANTAS  Frios ", FRIOS));
        assertEquals("SOUSA", NomeCidadeUtils.nomeDaRota("SOUSA", FRIOS));
        assertEquals("FRIOS", NomeCidadeUtils.nomeDaRota("FRIOS", FRIOS));
        assertEquals("SOUSA FRIOS", NomeCidadeUtils.nomeDaRota("SOUSA FRIOS", NENHUM));
    }

    @Test
    void nuloContinuaNulo() {
        assertNull(NomeCidadeUtils.chave(null, FRIOS));
        assertNull(NomeCidadeUtils.nomeDaRota(null, FRIOS));
        assertEquals(List.of(), NomeCidadeUtils.sufixos(""));
    }
}
