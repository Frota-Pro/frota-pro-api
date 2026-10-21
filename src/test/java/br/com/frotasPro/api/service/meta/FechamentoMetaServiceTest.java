package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.FechamentoMetaMotorista;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
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
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 9, 30), null, motorista));
    }

    @Test
    void cargaQueSaiuAntesDeConfirmarFicaNoMesDaSaidaMesmoFinalizadaDepois() {
        diasUteis(5);
        confirmacao(YearMonth.of(2026, 9), LocalDateTime.of(2026, 10, 2, 10, 0), true);

        // saiu 30/09, o motorista confirmou setembro em 02/10 e a carga so foi finalizada depois:
        // continua em setembro (o que conta e quando saiu)
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 9, 30), LocalDateTime.of(2026, 9, 30, 18, 0), motorista));
    }

    @Test
    void cargaQueSaiuNoFimDoMesFinalizadaDepoisDoPrazoContinuaNoMesDaSaida() {
        diasUteis(5);
        // saiu 29/10, voltou e finalizou depois do prazo (06/11): continua em outubro
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 10, 29), LocalDateTime.of(2026, 10, 29, 8, 0), motorista));
    }

    @Test
    void cargaMuitoAtrasadaContinuaNoMesDaSaida() {
        diasUteis(5);
        // saiu em julho, so finalizou em outubro: continua em julho
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 7, 15), LocalDateTime.of(2026, 7, 15, 8, 0), motorista));
    }

    @Test
    void cargaDoMesCorrenteNuncaMuda() {
        diasUteis(5);
        motoristaConfirmou(YearMonth.of(2026, 10), true);

        assertNull(service.calcularCompetencia(LocalDate.of(2026, 10, 12), null, motorista));
    }

    @Test
    void desativadoNuncaMudaACompetencia() {
        diasUteis(0);
        motoristaConfirmou(YearMonth.of(2026, 9), true);

        assertNull(service.calcularCompetencia(LocalDate.of(2026, 9, 28), null, motorista));
    }

    // ---- carga iniciada nos dias úteis a mais do mês anterior

    private void confirmacao(YearMonth mes, LocalDateTime em, boolean ativa) {
        FechamentoMetaMotorista f = new FechamentoMetaMotorista();
        f.setMotorista(motorista);
        f.setMesReferencia(mes.atDay(1));
        f.setConfirmadoEm(em);
        if (!ativa) {
            f.setReabertoEm(em.plusHours(1));
        }
        lenient().when(repository.findByMotoristaIdAndMesReferencia(motorista.getId(), mes.atDay(1)))
                .thenReturn(Optional.of(f));
    }

    @Test
    void cargaIniciadaNosDiasUteisAMaisContaNoMesAnteriorMesmoFinalizadaDepoisDoPrazo() {
        diasUteis(5);
        // outubro termina sábado 31/10: os 5 dias úteis vão de 02/11 a 06/11.
        // Saiu 03/11, só voltou e finalizou em 07/11 (o prazo já fechou em 06/11): conta em outubro.
        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 11, 3), LocalDateTime.of(2026, 11, 3, 8, 0),
                        motorista));
    }

    @Test
    void cargaIniciadaNoUltimoDiaDoPrazoAindaContaNoMesAnterior() {
        diasUteis(5);
        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 11, 6), LocalDateTime.of(2026, 11, 6, 23, 0),
                        motorista));
    }

    @Test
    void cargaIniciadaDepoisDoPrazoContaNoMesDaSaida() {
        diasUteis(5);
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 11, 9), LocalDateTime.of(2026, 11, 9, 8, 0),
                motorista));
    }

    @Test
    void cargaIniciadaDepoisDeConfirmarContaNoMesDaSaida() {
        diasUteis(5);
        confirmacao(YearMonth.of(2026, 10), LocalDateTime.of(2026, 11, 2, 10, 0), true);

        assertNull(service.calcularCompetencia(LocalDate.of(2026, 11, 3), LocalDateTime.of(2026, 11, 3, 8, 0),
                motorista));
    }

    @Test
    void cargaIniciadaAntesDeConfirmarFicaNoMesAnterior() {
        diasUteis(5);
        // saiu 03/11 de manhã; o motorista só confirmou outubro em 05/11
        confirmacao(YearMonth.of(2026, 10), LocalDateTime.of(2026, 11, 5, 10, 0), true);

        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 11, 3), LocalDateTime.of(2026, 11, 3, 8, 0),
                        motorista));
    }

    @Test
    void confirmacaoReabertaNaoImpedeACargaDeContarNoMesAnterior() {
        diasUteis(5);
        confirmacao(YearMonth.of(2026, 10), LocalDateTime.of(2026, 11, 2, 10, 0), false);

        assertEquals(LocalDate.of(2026, 10, 1),
                service.calcularCompetencia(LocalDate.of(2026, 11, 3), LocalDateTime.of(2026, 11, 3, 8, 0),
                        motorista));
    }

    @Test
    void desativadoIgnoraOsDiasUteisAMais() {
        diasUteis(0);
        assertNull(service.calcularCompetencia(LocalDate.of(2026, 11, 3), LocalDateTime.of(2026, 11, 3, 8, 0),
                motorista));
    }

    // ---- situacaoDoMes (regra única do status, do resumo e do relatório)

    @Test
    void situacaoDoMesComOsCincoDiasUteisDeOutubro() {
        YearMonth outubro = YearMonth.of(2026, 10); // termina sábado 31/10; prazo até sexta 06/11

        assertEquals("EM_ANDAMENTO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 10, 31), false));
        assertEquals("ABERTO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 11, 1), false));
        assertEquals("ABERTO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 11, 6), false));
        assertEquals("FECHADO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 11, 7), false));
    }

    @Test
    void situacaoDoMesConfirmadoSoValeDepoisQueOMesAcaba() {
        YearMonth outubro = YearMonth.of(2026, 10);

        assertEquals("CONFIRMADO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 11, 2), true));
        assertEquals("EM_ANDAMENTO", service.situacaoDoMes(5, outubro, LocalDate.of(2026, 10, 20), true));
    }

    @Test
    void situacaoDoMesConfirmadoGanhaDoPrazoVencido() {
        assertEquals("CONFIRMADO", service.situacaoDoMes(5, YearMonth.of(2026, 10), LocalDate.of(2026, 12, 1), true));
    }

    @Test
    void situacaoDoMesComParametroDesligado() {
        assertEquals("DESATIVADO", service.situacaoDoMes(0, YearMonth.of(2026, 10), LocalDate.of(2026, 11, 20), true));
    }
}
