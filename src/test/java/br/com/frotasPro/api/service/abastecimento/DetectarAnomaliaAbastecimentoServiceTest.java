package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.AbastecimentoRequerConfirmacaoException;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.AbastecimentoRepository.ReferenciaPrecoRow;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetectarAnomaliaAbastecimentoServiceTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 10, 20, 9, 0);

    @Mock
    private AbastecimentoRepository repository;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private ParametroSistemaService parametroSistemaService;
    @InjectMocks
    private DetectarAnomaliaAbastecimentoService service;

    private final UUID caminhaoId = UUID.randomUUID();
    private final UUID postoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ParametroSistema p = new ParametroSistema();
        p.setPercentualLimiteAnomaliaAbastecimento(25);
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(p);
    }

    private Abastecimento abastecimento() {
        Caminhao c = new Caminhao();
        c.setId(caminhaoId);
        c.setCodigo("CAM-000001");
        PostoAbastecimento posto = new PostoAbastecimento();
        posto.setId(postoId);

        Abastecimento a = new Abastecimento();
        a.setCaminhao(c);
        a.setPostoAbastecimento(posto);
        a.setDtAbastecimento(QUANDO);
        a.setKmOdometro(150_000);
        a.setTipoCombustivel(TipoCombustivel.DIESEL_COMUM);
        a.setValorLitro(new BigDecimal("5.00"));
        a.setValorTotal(new BigDecimal("400.00"));
        return a;
    }

    private ReferenciaPrecoRow ref(String mediaPreco, String mediaTotal, long amostras) {
        ReferenciaPrecoRow r = mock(ReferenciaPrecoRow.class);
        lenient().when(r.getMediaPreco()).thenReturn(mediaPreco == null ? null : new BigDecimal(mediaPreco));
        lenient().when(r.getMediaValorTotal()).thenReturn(mediaTotal == null ? null : new BigDecimal(mediaTotal));
        lenient().when(r.getAmostras()).thenReturn(amostras);
        return r;
    }

    private void referenciaDoPosto(ReferenciaPrecoRow r) {
        when(repository.referenciaPrecoPostoCombustivel(eq("DIESEL_COMUM"), any(), any(), any(), any(), any())).thenReturn(r);
    }

    // ---- odômetro repetido

    @Test
    void odometroRepetidoPedeConfirmacaoComAvisoDoKm() {
        Abastecimento a = abastecimento();
        when(repository.existeOutroComMesmoOdometro(caminhaoId, 150_000, null)).thenReturn(true);

        AbastecimentoRequerConfirmacaoException e = assertThrows(AbastecimentoRequerConfirmacaoException.class,
                () -> service.avaliar(a, false));

        assertEquals(1, e.getAvisos().size());
        assertTrue(e.getAvisos().get(0).contains("mesmo odômetro (150000 km)"));
    }

    @Test
    void odometroRepetidoConfirmadoSegueSemErro() {
        Abastecimento a = abastecimento();
        when(repository.existeOutroComMesmoOdometro(caminhaoId, 150_000, null)).thenReturn(true);

        assertDoesNotThrow(() -> service.avaliar(a, true));
    }

    @Test
    void naoChecaOdometroSemKmOuSemCaminhao() {
        Abastecimento semKm = abastecimento();
        semKm.setKmOdometro(null);
        service.avaliar(semKm, false);

        Abastecimento semCaminhao = abastecimento();
        semCaminhao.setCaminhao(null);
        service.avaliar(semCaminhao, false);

        verify(repository, never()).existeOutroComMesmoOdometro(any(), any(), any());
    }

    @Test
    void aoEditarExcluiOProprioRegistroDaChecagem() {
        Abastecimento a = abastecimento();
        UUID proprio = UUID.randomUUID();
        a.setId(proprio);

        service.avaliar(a, false);

        verify(repository).existeOutroComMesmoOdometro(caminhaoId, 150_000, proprio);
    }

    @Test
    void idExplicitoTemPrioridadeSobreOIdDoObjeto() {
        Abastecimento a = abastecimento();
        UUID antigo = UUID.randomUUID();

        service.avaliar(a, false, antigo);

        verify(repository).existeOutroComMesmoOdometro(caminhaoId, 150_000, antigo);
    }

    // ---- preço do litro

    @Test
    void precoDentroDoLimiteNaoAvisaEGravaAMediaDeReferencia() {
        Abastecimento a = abastecimento();
        a.setPrecoAnomalo(true); // lixo de uma avaliação anterior precisa ser zerado
        referenciaDoPosto(ref("4.80", "380", 10));

        service.avaliar(a, false);

        assertFalse(a.isPrecoAnomalo());
        assertNull(a.getPrecoAnomaloPercentual());
        assertEquals(new BigDecimal("4.800"), a.getPrecoMedioReferencia());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void precoAcimaDoLimitePedeConfirmacaoEMarcaOPercentual() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("6.50")); // 30% acima de 5,00
        referenciaDoPosto(ref("5.00", "400", 10));

        AbastecimentoRequerConfirmacaoException e = assertThrows(AbastecimentoRequerConfirmacaoException.class,
                () -> service.avaliar(a, false));

        assertTrue(e.getAvisos().get(0).contains("30% acima da média"));
        assertTrue(a.isPrecoAnomalo());
        assertEquals(new BigDecimal("30.0"), a.getPrecoAnomaloPercentual());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void precoAnomaloConfirmadoNotificaOAdmin() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("6.50"));
        referenciaDoPosto(ref("5.00", "400", 10));

        service.avaliar(a, true);

        assertTrue(a.isPrecoAnomalo());
        verify(notificacaoService).notificar(eq(EventoNotificacao.ABASTECIMENTO_PRECO_ANOMALO),
                eq(TipoNotificacao.ALERTA), anyString(), anyString(), eq("ABASTECIMENTO"), isNull(), eq("CAM-000001"));
    }

    @Test
    void exatamenteNoLimiteAindaNaoEAnomalo() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("6.25")); // exatamente 25% acima de 5,00
        referenciaDoPosto(ref("5.00", "400", 10));

        service.avaliar(a, false);

        assertFalse(a.isPrecoAnomalo());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void limiteVemDoParametroDoSistema() {
        ParametroSistema p = new ParametroSistema();
        p.setPercentualLimiteAnomaliaAbastecimento(10);
        when(parametroSistemaService.buscarOuPadrao()).thenReturn(p);
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("5.60")); // 12% acima
        referenciaDoPosto(ref("5.00", "400", 10));

        assertThrows(AbastecimentoRequerConfirmacaoException.class, () -> service.avaliar(a, false));
    }

    // ---- valor total

    @Test
    void valorTotalMuitoAcimaDaMediaAvisaMasNaoMarcaPrecoAnomalo() {
        Abastecimento a = abastecimento();
        a.setValorTotal(new BigDecimal("600.00")); // 50% acima de 400
        referenciaDoPosto(ref("5.00", "400", 10));

        AbastecimentoRequerConfirmacaoException e = assertThrows(AbastecimentoRequerConfirmacaoException.class,
                () -> service.avaliar(a, false));

        assertTrue(e.getAvisos().get(0).contains("Valor total"));
        assertFalse(a.isPrecoAnomalo());
    }

    @Test
    void precoEValorTotalAnomalosGeramDoisAvisos() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("7.00"));
        a.setValorTotal(new BigDecimal("700.00"));
        referenciaDoPosto(ref("5.00", "400", 10));

        AbastecimentoRequerConfirmacaoException e = assertThrows(AbastecimentoRequerConfirmacaoException.class,
                () -> service.avaliar(a, false));

        assertEquals(2, e.getAvisos().size());
    }

    // ---- referência: posto x frota

    @Test
    void postoSemHistoricoSuficienteUsaAMediaDaFrota() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("6.50"));
        referenciaDoPosto(ref("5.00", "400", 2)); // menos de 3 amostras no posto
        ReferenciaPrecoRow frota = ref("5.00", "400", 5);
        when(repository.referenciaPrecoCombustivelFrota(eq("DIESEL_COMUM"), any(), any(), any())).thenReturn(frota);

        assertThrows(AbastecimentoRequerConfirmacaoException.class, () -> service.avaliar(a, false));
        assertTrue(a.isPrecoAnomalo());
    }

    @Test
    void semAmostrasSuficientesNemNaFrotaNaoChecaPreco() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("50.00"));
        referenciaDoPosto(ref("5.00", "400", 1));
        ReferenciaPrecoRow frota = ref("5.00", "400", 4); // a frota precisa de 5 amostras
        when(repository.referenciaPrecoCombustivelFrota(eq("DIESEL_COMUM"), any(), any(), any())).thenReturn(frota);

        assertDoesNotThrow(() -> service.avaliar(a, false));
        assertFalse(a.isPrecoAnomalo());
        assertNull(a.getPrecoMedioReferencia());
    }

    @Test
    void postoComHistoricoSuficienteNaoConsultaAFrota() {
        Abastecimento a = abastecimento();
        referenciaDoPosto(ref("5.00", "400", 3));

        service.avaliar(a, false);

        verify(repository, never()).referenciaPrecoCombustivelFrota(any(), any(), any(), any());
    }

    @Test
    void mediaDePrecoInvalidaIgnoraAChecagem() {
        Abastecimento a = abastecimento();
        a.setValorLitro(new BigDecimal("50.00"));
        referenciaDoPosto(ref("0", "400", 10));

        assertDoesNotThrow(() -> service.avaliar(a, false));
        assertFalse(a.isPrecoAnomalo());
    }

    @Test
    void janelaDeReferenciaSao90DiasAteOMomentoDoAbastecimento() {
        Abastecimento a = abastecimento();
        referenciaDoPosto(ref("5.00", "400", 10));

        service.avaliar(a, false);

        verify(repository).referenciaPrecoPostoCombustivel("DIESEL_COMUM", QUANDO.minusDays(90), QUANDO, null, postoId, null);
    }

    // ---- dados incompletos

    @Test
    void semPostoIdentificavelNaoConsultaReferenciaDePreco() {
        Abastecimento a = abastecimento();
        a.setPostoAbastecimento(null);
        a.setPosto("   ");

        service.avaliar(a, false);

        verify(repository, never()).referenciaPrecoPostoCombustivel(any(), any(), any(), any(), any(), any());
    }

    @Test
    void postoSoPeloNomeUsaOTextoSemEspacos() {
        Abastecimento a = abastecimento();
        a.setPostoAbastecimento(null);
        a.setPosto("  Posto Central ");
        referenciaDoPosto(ref("5.00", "400", 10));

        service.avaliar(a, false);

        verify(repository).referenciaPrecoPostoCombustivel("DIESEL_COMUM", QUANDO.minusDays(90), QUANDO, null, null, "Posto Central");
    }

    @Test
    void semPrecoTipoOuDataNaoChecaPreco() {
        Abastecimento semPreco = abastecimento();
        semPreco.setValorLitro(null);
        Abastecimento semTipo = abastecimento();
        semTipo.setTipoCombustivel(null);
        Abastecimento semData = abastecimento();
        semData.setDtAbastecimento(null);

        service.avaliar(semPreco, false);
        service.avaliar(semTipo, false);
        service.avaliar(semData, false);

        verify(repository, never()).referenciaPrecoPostoCombustivel(any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(notificacaoService);
    }
}
