package br.com.frotasPro.api.utils;

import br.com.frotasPro.api.excption.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoValidatorTest {

    private static final LocalDate D1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate D2 = LocalDate.of(2026, 10, 31);

    @Test
    void periodoObrigatorioAceitaInicioIgualOuAnteriorAoFim() {
        assertDoesNotThrow(() -> PeriodoValidator.obrigatorio(D1, D2, "relatório"));
        assertDoesNotThrow(() -> PeriodoValidator.obrigatorio(D1, D1, "relatório"));
    }

    @Test
    void periodoObrigatorioRecusaDatasNulas() {
        BusinessException e = assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio(null, D2, "relatório"));
        assertTrue(e.getMessage().contains("Informe as datas de início e fim"));
        assertTrue(e.getMessage().contains("(relatório)"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio(D1, null, null));
    }

    @Test
    void periodoObrigatorioRecusaInicioDepoisDoFim() {
        BusinessException e = assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio(D2, D1, "meta"));
        assertTrue(e.getMessage().contains("início não pode ser maior que fim"));
    }

    @Test
    void periodoOpcionalAceitaAmbosVazios() {
        assertDoesNotThrow(() -> PeriodoValidator.opcional((LocalDate) null, null, "filtro"));
        assertDoesNotThrow(() -> PeriodoValidator.opcional(D1, D2, "filtro"));
    }

    @Test
    void periodoOpcionalRecusaSoUmaDasDatas() {
        BusinessException e = assertThrows(BusinessException.class, () -> PeriodoValidator.opcional(D1, null, "filtro"));
        assertTrue(e.getMessage().contains("ou deixe ambos vazios"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.opcional((LocalDate) null, D2, "filtro"));
    }

    @Test
    void periodoOpcionalRecusaInicioDepoisDoFim() {
        assertThrows(BusinessException.class, () -> PeriodoValidator.opcional(D2, D1, "filtro"));
    }

    @Test
    void versaoComDataEHoraSeguemAsMesmasRegras() {
        LocalDateTime a = LocalDateTime.of(2026, 10, 1, 8, 0);
        LocalDateTime b = LocalDateTime.of(2026, 10, 1, 18, 0);

        assertDoesNotThrow(() -> PeriodoValidator.obrigatorio(a, b, "turno"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio(b, a, "turno"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio((LocalDateTime) null, b, "turno"));
        assertDoesNotThrow(() -> PeriodoValidator.opcional((LocalDateTime) null, null, "turno"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.opcional(a, null, "turno"));
        assertThrows(BusinessException.class, () -> PeriodoValidator.opcional(b, a, "turno"));
    }

    @Test
    void nomeDoCampoEmBrancoNaoApareceEntreParenteses() {
        BusinessException e = assertThrows(BusinessException.class, () -> PeriodoValidator.obrigatorio(D2, D1, "  "));
        assertFalse(e.getMessage().contains("("));
    }
}
