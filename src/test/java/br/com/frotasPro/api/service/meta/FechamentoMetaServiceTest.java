package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.repository.FechamentoMetaMotoristaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.DiaUtilUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FechamentoMetaServiceTest {

    @Mock
    private FechamentoMetaMotoristaRepository repository;
    @Mock
    private ParametroSistemaService parametroSistemaService;

    private FechamentoMetaService service;
    private Motorista motorista;

    @BeforeEach
    void setUp() {
        service = new FechamentoMetaService(repository, parametroSistemaService);
        motorista = new Motorista();
        motorista.setId(UUID.randomUUID());
    }

    private void diasUteis(int n) {
        ParametroSistema p = new ParametroSistema();
        p.setDiasUteisFechamentoMeta(n);
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(p);
    }

    private void motoristaConfirmou(YearMonth mes, boolean confirmou) {
        lenient().when(repository.existsByMotoristaIdAndMesReferenciaAndReabertoEmIsNull(motorista.getId(), mes.atDay(1)))
                .thenReturn(confirmou);
    }

    private Meta metaMensal(YearMonth mes) {
        Meta meta = new Meta();
        meta.setDataIncio(mes.atDay(1));
        meta.setDataFim(mes.atEndOfMonth());
        meta.setMotorista(motorista);
        return meta;
    }

    // ---- dia útil

    @Test
    void enesimoDiaUtilPulaFimDeSemana() {
        // 30/09/2026 é quarta: 01/10 qui, 02/10 sex, 05/10 seg, 06/10 ter, 07/10 qua
        assertEquals(LocalDate.of(2026, 10, 7), DiaUtilUtils.enesimoDiaUtilApos(LocalDate.of(2026, 9, 30), 5));
        // 31/07/2026 é sexta: o 1º dia útil depois é segunda 03/08
        assertEquals(LocalDate.of(2026, 8, 3), DiaUtilUtils.enesimoDiaUtilApos(LocalDate.of(2026, 7, 31), 1));
        assertEquals(LocalDate.of(2026, 9, 30), DiaUtilUtils.enesimoDiaUtilApos(LocalDate.of(2026, 9, 30), 0));
    }

    // ---- prazo geral

    @Test
    void mesContinuaAbertoAteOQuintoDiaUtilDoMesSeguinte() {
        diasUteis(5);
        assertEquals(LocalDate.of(2026, 10, 7), service.ultimoDiaAberto(YearMonth.of(2026, 9)));
    }

    @Test
    void desativadoMantemOFimDoMes() {
        diasUteis(0);
        assertEquals(LocalDate.of(2026, 9, 30), service.ultimoDiaAberto(YearMonth.of(2026, 9)));
        assertFalse(service.isAtivo());
    }

    @Test
    void metaMensalSoFechaDepoisDoPrazo() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 9), false);
        Meta setembro = metaMensal(YearMonth.of(2026, 9));

        assertFalse(service.metaFechada(setembro, LocalDate.of(2026, 10, 1)));
        assertFalse(service.metaFechada(setembro, LocalDate.of(2026, 10, 7)));
        assertTrue(service.metaFechada(setembro, LocalDate.of(2026, 10, 8)));
    }

    @Test
    void metaMensalFechaNoDiaSeguinteSeDesativado() {
        diasUteis(0);
        Meta setembro = metaMensal(YearMonth.of(2026, 9));

        assertFalse(service.metaFechada(setembro, LocalDate.of(2026, 9, 30)));
        assertTrue(service.metaFechada(setembro, LocalDate.of(2026, 10, 1)));
    }

    @Test
    void metaDePeriodoLivreNaoGanhaFolga() {
        diasUteis(5);
        Meta quinzena = new Meta();
        quinzena.setDataIncio(LocalDate.of(2026, 9, 10));
        quinzena.setDataFim(LocalDate.of(2026, 9, 25));

        assertTrue(service.metaFechada(quinzena, LocalDate.of(2026, 9, 26)));
    }

    @Test
    void motoristaQueConfirmaFechaAMetaDeleAntesDoPrazo() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 9), true);
        Meta setembro = metaMensal(YearMonth.of(2026, 9));

        assertTrue(service.metaFechada(setembro, LocalDate.of(2026, 10, 2)));
    }

    // ---- competência da carga

    @Test
    void cargaFinalizadaDentroDoPrazoContaNoMesDaSaida() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 9), false);

        // saiu dia 30/09, voltou e finalizou em 03/10 (prazo vai até 07/10)
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 9, 30), motorista, LocalDate.of(2026, 10, 3)));
    }

    @Test
    void cargaFinalizadaDepoisDeConfirmarVaiParaOMesSeguinte() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 9), true);

        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 9, 30), motorista, LocalDate.of(2026, 10, 3)));
    }

    @Test
    void cargaFinalizadaDepoisDoPrazoGeralVaiParaOMesSeguinte() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 9), false);

        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 9, 28), motorista, LocalDate.of(2026, 10, 20)));
    }

    @Test
    void cargaMuitoAtrasadaVaiParaOPrimeiroMesAbertoNaoParaOMesSeguinteFechado() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 7), false);
        motoristaConfirmou(YearMonth.of(2026, 8), false);
        motoristaConfirmou(YearMonth.of(2026, 9), false);

        // saiu em julho, só finalizou em 20/10: julho, agosto e setembro já fecharam -> outubro
        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 7, 15), motorista, LocalDate.of(2026, 10, 20)));
    }

    @Test
    void cargaDoMesCorrenteNuncaMuda() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 10), true);

        assertNull(service.calcularCompetencia(LocalDate.of(2026, 10, 5), motorista, LocalDate.of(2026, 10, 6)));
    }

    @Test
    void desativadoNuncaMudaACompetencia() {
        diasUteis(0);
        motoristaConfirmou(YearMonth.of(2026, 9), true);

        assertNull(service.calcularCompetencia(LocalDate.of(2026, 9, 28), motorista, LocalDate.of(2026, 11, 20)));
    }
}
