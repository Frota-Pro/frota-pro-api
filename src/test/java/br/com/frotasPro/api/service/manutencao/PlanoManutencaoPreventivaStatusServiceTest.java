package br.com.frotasPro.api.service.manutencao;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.PlanoManutencaoPreventiva;
import br.com.frotasPro.api.service.manutencao.PlanoManutencaoPreventivaStatusService.Situacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanoManutencaoPreventivaStatusServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 20);

    private final PlanoManutencaoPreventivaStatusService service = new PlanoManutencaoPreventivaStatusService();
    private ParametroSistema parametro;

    @BeforeEach
    void setUp() {
        parametro = new ParametroSistema();
        parametro.setKmAntecedenciaManutencaoPreventiva(500);
        parametro.setDiasAntecedenciaVencimentoDocumento(5);
    }

    private PlanoManutencaoPreventiva planoPorKm(Integer odometro, Integer ultimoKm, Integer intervaloKm) {
        Caminhao c = new Caminhao();
        c.setOdometroUltimaCarga(odometro);
        PlanoManutencaoPreventiva p = new PlanoManutencaoPreventiva();
        p.setCaminhao(c);
        p.setUltimoKmExecutado(ultimoKm);
        p.setIntervaloKm(intervaloKm);
        return p;
    }

    private PlanoManutencaoPreventiva planoPorData(LocalDate ultimaData, Integer intervaloDias) {
        PlanoManutencaoPreventiva p = new PlanoManutencaoPreventiva();
        p.setCaminhao(new Caminhao());
        p.setUltimaDataExecutada(ultimaData);
        p.setIntervaloDias(intervaloDias);
        return p;
    }

    // ---- por km (próxima revisão em 110.000 + 10.000 = 120.000)

    @Test
    void emDiaQuandoFaltaMaisQueAAntecedenciaEmKm() {
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorKm(115_000, 110_000, 10_000), HOJE, parametro));
    }

    @Test
    void vencendoQuandoEstaDentroDaAntecedenciaEmKm() {
        // 119.500 = exatamente 500 km antes de 120.000
        assertEquals(Situacao.VENCENDO, service.calcularSituacao(planoPorKm(119_500, 110_000, 10_000), HOJE, parametro));
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorKm(119_499, 110_000, 10_000), HOJE, parametro));
    }

    @Test
    void vencidoQuandoOdometroChegaNoProximoKm() {
        assertEquals(Situacao.VENCIDO, service.calcularSituacao(planoPorKm(120_000, 110_000, 10_000), HOJE, parametro));
        assertEquals(Situacao.VENCIDO, service.calcularSituacao(planoPorKm(125_000, 110_000, 10_000), HOJE, parametro));
    }

    @Test
    void antecedenciaDeKmVemDoParametro() {
        parametro.setKmAntecedenciaManutencaoPreventiva(2_000);

        assertEquals(Situacao.VENCENDO, service.calcularSituacao(planoPorKm(118_000, 110_000, 10_000), HOJE, parametro));
    }

    @Test
    void semDadosDeKmNaoCalculaPorKm() {
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorKm(null, 110_000, 10_000), HOJE, parametro));
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorKm(125_000, null, 10_000), HOJE, parametro));
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorKm(125_000, 110_000, null), HOJE, parametro));
    }

    @Test
    void planoSemCaminhaoNaoQuebra() {
        PlanoManutencaoPreventiva p = new PlanoManutencaoPreventiva();
        p.setIntervaloKm(10_000);
        p.setUltimoKmExecutado(100_000);

        assertEquals(Situacao.EM_DIA, service.calcularSituacao(p, HOJE, parametro));
    }

    // ---- por data (última em 01/10 + 30 dias = 31/10)

    @Test
    void emDiaQuandoAindaFaltaMaisQueAAntecedenciaEmDias() {
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorData(LocalDate.of(2026, 10, 1), 30), HOJE, parametro));
    }

    @Test
    void vencendoQuandoEstaNaJanelaDeAntecedenciaEmDias() {
        // próxima = 31/10; janela de 5 dias começa em 26/10
        assertEquals(Situacao.VENCENDO, service.calcularSituacao(planoPorData(LocalDate.of(2026, 10, 1), 30),
                LocalDate.of(2026, 10, 26), parametro));
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorData(LocalDate.of(2026, 10, 1), 30),
                LocalDate.of(2026, 10, 25), parametro));
    }

    @Test
    void vencidoNoDiaDaProximaDataOuDepois() {
        PlanoManutencaoPreventiva plano = planoPorData(LocalDate.of(2026, 10, 1), 30);

        assertEquals(Situacao.VENCIDO, service.calcularSituacao(plano, LocalDate.of(2026, 10, 31), parametro));
        assertEquals(Situacao.VENCIDO, service.calcularSituacao(plano, LocalDate.of(2026, 11, 15), parametro));
    }

    @Test
    void semDadosDeDataNaoCalculaPorData() {
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorData(null, 30), HOJE, parametro));
        assertEquals(Situacao.EM_DIA, service.calcularSituacao(planoPorData(LocalDate.of(2020, 1, 1), null), HOJE, parametro));
    }

    // ---- combinação

    @Test
    void valeOQueVencerPrimeiroKmOuData() {
        PlanoManutencaoPreventiva plano = planoPorKm(125_000, 110_000, 10_000); // vencido por km
        plano.setUltimaDataExecutada(LocalDate.of(2026, 10, 19));               // em dia por data
        plano.setIntervaloDias(180);

        assertEquals(Situacao.VENCIDO, service.calcularSituacao(plano, HOJE, parametro));

        PlanoManutencaoPreventiva porData = planoPorKm(111_000, 110_000, 10_000); // em dia por km
        porData.setUltimaDataExecutada(LocalDate.of(2026, 1, 1));                 // vencido por data
        porData.setIntervaloDias(30);

        assertEquals(Situacao.VENCIDO, service.calcularSituacao(porData, HOJE, parametro));
    }

    @Test
    void vencidoGanhaDeVencendoQuandoUmCriterioEmCadaEstado() {
        PlanoManutencaoPreventiva plano = planoPorKm(119_600, 110_000, 10_000); // vencendo por km
        plano.setUltimaDataExecutada(LocalDate.of(2026, 1, 1));                 // vencido por data
        plano.setIntervaloDias(30);

        assertEquals(Situacao.VENCIDO, service.calcularSituacao(plano, HOJE, parametro));
    }

    // ---- usado pelo job de notificação

    @Test
    void estaVencendoValeParaVencendoEVencidoMasNaoParaEmDia() {
        assertFalse(service.estaVencendo(planoPorKm(100_000, 110_000, 10_000), HOJE, parametro));
        assertTrue(service.estaVencendo(planoPorKm(119_600, 110_000, 10_000), HOJE, parametro));
        assertTrue(service.estaVencendo(planoPorKm(121_000, 110_000, 10_000), HOJE, parametro));
    }
}
