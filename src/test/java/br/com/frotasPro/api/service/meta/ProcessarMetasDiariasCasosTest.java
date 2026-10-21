package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.NotificacaoRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.util.MetaProgressoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Casos da rotina diária das metas (a que roda 1h da manhã e no "processar agora"). */
@ExtendWith(MockitoExtension.class)
class ProcessarMetasDiariasCasosTest {

    @Mock
    private MetaRepository metaRepository;
    @Mock
    private MetaCategoriaCaminhaoVinculoService vinculoService;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private MetaProgressoService metaProgressoService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private FechamentoMetaService fechamentoMetaService;

    private ProcessarMetasDiariasService service;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        service = new ProcessarMetasDiariasService(metaRepository, vinculoService, caminhaoRepository,
                metaProgressoService, notificacaoService, notificacaoRepository, fechamentoMetaService);
        hoje = FusoHorarioUtils.hojeBrasil();

        lenient().when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of());
        lenient().when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of());
        lenient().when(metaRepository.findByDataFimBeforeAndStatusMeta(any(), eq(StatusMeta.EM_ANDAMENTO))).thenReturn(List.of());
        lenient().when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of());
        lenient().when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Meta meta(LocalDate inicio, LocalDate fim, StatusMeta status) {
        Meta m = new Meta();
        m.setId(UUID.randomUUID());
        m.setDataIncio(inicio);
        m.setDataFim(fim);
        m.setStatusMeta(status);
        m.setTipoMeta(TipoMeta.TONELADA);
        m.setValorMeta(new BigDecimal("100"));
        m.setValorRealizado(new BigDecimal("40"));
        m.setUnidade("t");
        m.setDescricao("Meta de teste");
        return m;
    }

    // ---- status automático

    @Test
    void metaQueAindaNaoComecouFicaNaoIniciada() {
        Meta futura = meta(hoje.plusDays(5), hoje.plusDays(35), StatusMeta.EM_ANDAMENTO);
        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(futura));

        service.processar();

        assertEquals(StatusMeta.NAO_INICIADA, futura.getStatusMeta());
    }

    @Test
    void metaNoPeriodoAindaAbertaFicaEmAndamento() {
        Meta m = meta(hoje.minusDays(5), hoje.plusDays(10), StatusMeta.NAO_INICIADA);
        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(m));
        when(fechamentoMetaService.metaFechada(m, hoje)).thenReturn(false);

        service.processar();

        assertEquals(StatusMeta.EM_ANDAMENTO, m.getStatusMeta());
    }

    @Test
    void metaFechadaPeloPrazoOuPelaConfirmacaoFicaConcluida() {
        Meta m = meta(hoje.minusDays(40), hoje.minusDays(10), StatusMeta.EM_ANDAMENTO);
        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(m));
        when(fechamentoMetaService.metaFechada(m, hoje)).thenReturn(true);

        service.processar();

        assertEquals(StatusMeta.CONCLUIDA, m.getStatusMeta());
    }

    @Test
    void metaVencidaMasAindaDentroDoPrazoDeFechamentoContinuaEmAndamento() {
        Meta m = meta(hoje.minusDays(40), hoje.minusDays(3), StatusMeta.EM_ANDAMENTO);
        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(m));
        when(metaRepository.findByDataFimBeforeAndStatusMeta(hoje, StatusMeta.EM_ANDAMENTO)).thenReturn(List.of(m));
        when(fechamentoMetaService.metaFechada(m, hoje)).thenReturn(false);

        service.processar();

        assertEquals(StatusMeta.EM_ANDAMENTO, m.getStatusMeta());
    }

    @Test
    void metaSemDatasNaoMudaDeStatus() {
        Meta m = meta(null, null, StatusMeta.EM_ANDAMENTO);
        when(metaRepository.findByStatusMetaNot(StatusMeta.CANCELADA)).thenReturn(List.of(m));

        service.processar();

        assertEquals(StatusMeta.EM_ANDAMENTO, m.getStatusMeta());
        verify(fechamentoMetaService, never()).metaFechada(any(), any());
    }

    // ---- renovação

    private Meta metaDoMesPassado(Caminhao caminhao) {
        LocalDate inicioMesPassado = hoje.minusMonths(1).withDayOfMonth(1);
        Meta m = meta(inicioMesPassado, inicioMesPassado.withDayOfMonth(inicioMesPassado.lengthOfMonth()), StatusMeta.CONCLUIDA);
        m.setCaminhao(caminhao);
        m.setRenovarAutomaticamente(true);
        return m;
    }

    @Test
    void renovaCopiandoOsDadosEZerandoORealizado() {
        Caminhao c = new Caminhao();
        Meta antiga = metaDoMesPassado(c);
        when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of(antiga));
        when(metaRepository.existsMetaNaoCanceladaConflitante(any(), any(), any(), any(), any(), any(), any())).thenReturn(false);

        service.processar();

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        Meta nova = salva.getValue();
        assertEquals(antiga.getDataFim().plusDays(1), nova.getDataIncio());
        assertEquals(TipoMeta.TONELADA, nova.getTipoMeta());
        assertEquals(new BigDecimal("100"), nova.getValorMeta());
        assertEquals(BigDecimal.ZERO, nova.getValorRealizado());
        assertEquals("t", nova.getUnidade());
        assertEquals("Meta de teste", nova.getDescricao());
        assertSame(c, nova.getCaminhao());
        assertEquals(true, nova.isRenovarAutomaticamente());
        verify(vinculoService).sincronizar(nova);
    }

    @Test
    void metaDoMesQueEstaRodandoNasceEmAndamento() {
        Meta antiga = metaDoMesPassado(new Caminhao());
        when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of(antiga));

        service.processar();

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(StatusMeta.EM_ANDAMENTO, salva.getValue().getStatusMeta());
    }

    @Test
    void naoRenovaMetaSemRenovacaoAutomatica() {
        Meta antiga = metaDoMesPassado(new Caminhao());
        antiga.setRenovarAutomaticamente(false);
        when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of(antiga));

        service.processar();

        verify(metaRepository, never()).save(any(Meta.class));
        verifyNoInteractions(vinculoService);
    }

    @Test
    void naoRenovaSeJaExisteMetaNoPeriodoSeguinte() {
        Meta antiga = metaDoMesPassado(new Caminhao());
        when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of(antiga));
        when(metaRepository.existsMetaNaoCanceladaConflitante(any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        service.processar();

        verify(metaRepository, never()).save(any(Meta.class));
    }

    @Test
    void renovaMetaDeMotoristaEDeCategoriaCopiandoOAlvo() {
        Motorista mot = new Motorista();
        Meta antiga = metaDoMesPassado(null);
        antiga.setMotorista(mot);
        CategoriaCaminhao cat = new CategoriaCaminhao();
        Meta daCategoria = metaDoMesPassado(null);
        daCategoria.setCategoria(cat);
        when(metaRepository.findByDataFimBeforeAndStatusMetaIn(any(), any())).thenReturn(List.of(antiga, daCategoria));

        service.processar();

        ArgumentCaptor<Meta> salvas = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository, org.mockito.Mockito.times(2)).save(salvas.capture());
        assertSame(mot, salvas.getAllValues().get(0).getMotorista());
        assertSame(cat, salvas.getAllValues().get(1).getCategoria());
    }

    // ---- fim do período da renovação

    @Test
    void mesCalendarioCompletoRenovaParaOMesInteiroSeguinte() {
        Meta fevereiro = meta(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), StatusMeta.CONCLUIDA);
        Meta dezembro = meta(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), StatusMeta.CONCLUIDA);

        assertEquals(LocalDate.of(2026, 3, 31), service.calcularDataFimRenovacao(fevereiro, LocalDate.of(2026, 3, 1)));
        assertEquals(LocalDate.of(2027, 1, 31), service.calcularDataFimRenovacao(dezembro, LocalDate.of(2027, 1, 1)));
    }

    @Test
    void anoBissextoRespeitaOUltimoDiaDeFevereiro() {
        Meta janeiro = meta(LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 31), StatusMeta.CONCLUIDA);

        assertEquals(LocalDate.of(2028, 2, 29), service.calcularDataFimRenovacao(janeiro, LocalDate.of(2028, 2, 1)));
    }

    @Test
    void periodoLivreMantemAMesmaDuracao() {
        // 10 a 25 de março = 15 dias de diferença; a nova começa em 26/03 e dura o mesmo
        Meta livre = meta(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 25), StatusMeta.CONCLUIDA);

        assertEquals(LocalDate.of(2026, 4, 10), service.calcularDataFimRenovacao(livre, LocalDate.of(2026, 3, 26)));
    }

    @Test
    void mesIncompletoNaoEMesCalendario() {
        Meta quase = meta(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 30), StatusMeta.CONCLUIDA);

        // 01 a 30/03 (29 dias de diferença): a nova começa em 31/03 e dura o mesmo, até 29/04
        assertEquals(LocalDate.of(2026, 4, 29), service.calcularDataFimRenovacao(quase, LocalDate.of(2026, 3, 31)));
    }

    // ---- caminhão fora da meta

    private Meta metaDeCategoriaVencendo() {
        CategoriaCaminhao cat = new CategoriaCaminhao();
        cat.setId(UUID.randomUUID());
        cat.setCodigo("CAT-1");
        Meta m = meta(hoje.minusDays(20), hoje.plusDays(2), StatusMeta.EM_ANDAMENTO);
        m.setCategoria(cat);
        return m;
    }

    private Caminhao caminhao(String codigo) {
        Caminhao c = new Caminhao();
        c.setId(UUID.randomUUID());
        c.setCodigo(codigo);
        return c;
    }

    @Test
    void avisaOCaminhaoDaCategoriaQueNaoBateuAMetaPertoDoFim() {
        Meta m = metaDeCategoriaVencendo();
        Caminhao c = caminhao("CAM-1");
        when(metaRepository.findByStatusMetaInAndDataFimBetween(List.of(StatusMeta.EM_ANDAMENTO), hoje, hoje.plusDays(3)))
                .thenReturn(List.of(m));
        when(caminhaoRepository.findByCategoriaIdAndAtivoTrue(m.getCategoria().getId())).thenReturn(List.of(c));
        when(metaProgressoService.calcularValorRealizado(m, c, null)).thenReturn(new BigDecimal("30"));
        when(metaProgressoService.metaAtingida(TipoMeta.TONELADA, new BigDecimal("30"), new BigDecimal("100"))).thenReturn(false);

        service.processar();

        verify(notificacaoService).notificar(eq(EventoNotificacao.META_CAMINHAO_FORA), eq(TipoNotificacao.ALERTA),
                eq("Caminhão fora da meta"), anyString(), eq("META"), eq(m.getId()), eq(m.getId() + "|CAM-1"));
    }

    @Test
    void naoAvisaQuemJaBateuAMeta() {
        Meta m = metaDeCategoriaVencendo();
        Caminhao c = caminhao("CAM-1");
        when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of(m));
        when(caminhaoRepository.findByCategoriaIdAndAtivoTrue(any())).thenReturn(List.of(c));
        when(metaProgressoService.calcularValorRealizado(m, c, null)).thenReturn(new BigDecimal("120"));
        when(metaProgressoService.metaAtingida(any(), any(), any())).thenReturn(true);

        service.processar();

        verifyNoInteractions(notificacaoService);
    }

    @Test
    void naoAvisaDeNovoNoMesmoDia() {
        Meta m = metaDeCategoriaVencendo();
        Caminhao c = caminhao("CAM-1");
        when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of(m));
        when(caminhaoRepository.findByCategoriaIdAndAtivoTrue(any())).thenReturn(List.of(c));
        when(metaProgressoService.calcularValorRealizado(m, c, null)).thenReturn(BigDecimal.ONE);
        when(metaProgressoService.metaAtingida(any(), any(), any())).thenReturn(false);
        when(notificacaoRepository.existsByEventoAndReferenciaIdAndReferenciaCodigoAndCriadoEmAfter(
                eq(EventoNotificacao.META_CAMINHAO_FORA), eq(m.getId()), eq(m.getId() + "|CAM-1"), eq(hoje.atStartOfDay())))
                .thenReturn(true);

        service.processar();

        verifyNoInteractions(notificacaoService);
    }

    @Test
    void metaSemCategoriaNaoGeraAvisoPorCaminhao() {
        Meta m = meta(hoje.minusDays(20), hoje.plusDays(2), StatusMeta.EM_ANDAMENTO);
        when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of(m));

        service.processar();

        verifyNoInteractions(caminhaoRepository, notificacaoService);
    }

    @Test
    void cadaCaminhaoDaCategoriaTemOSeuProprioAviso() {
        Meta m = metaDeCategoriaVencendo();
        Caminhao a = caminhao("CAM-A");
        Caminhao b = caminhao("CAM-B");
        when(metaRepository.findByStatusMetaInAndDataFimBetween(any(), any(), any())).thenReturn(List.of(m));
        when(caminhaoRepository.findByCategoriaIdAndAtivoTrue(any())).thenReturn(List.of(a, b));
        when(metaProgressoService.calcularValorRealizado(any(Meta.class), any(Caminhao.class), any())).thenReturn(BigDecimal.ONE);
        when(metaProgressoService.metaAtingida(any(), any(), any())).thenReturn(false);

        service.processar();

        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), anyString(), eq(m.getId()), eq(m.getId() + "|CAM-A"));
        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), anyString(), eq(m.getId()), eq(m.getId() + "|CAM-B"));
    }
}
