package br.com.frotasPro.api.service.abastecimento;

import br.com.frotasPro.api.controller.request.AbastecimentoRequest;
import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.PostoAbastecimento;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.AbastecimentoRequerConfirmacaoException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AbastecimentoRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.CalcularMediaKmLitroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarAbastecimentoServiceTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 10, 20, 9, 0);

    @Mock
    private AbastecimentoRepository repository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private CalcularMediaKmLitroService calcularMediaKmLitroService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private ResolverPostoAbastecimentoService resolverPostoAbastecimentoService;
    @Mock
    private DetectarAnomaliaAbastecimentoService detectarAnomaliaAbastecimentoService;
    @InjectMocks
    private CriarAbastecimentoService service;

    private Caminhao caminhao;
    private Motorista titular;

    @BeforeEach
    void setUp() {
        titular = new Motorista();
        titular.setCodigo("MOT-TIT");
        caminhao = new Caminhao();
        caminhao.setCodigo("CAM-000001");
        caminhao.setMotoristaTitular(titular);

        lenient().when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-000001")).thenReturn(Optional.of(caminhao));
        lenient().when(repository.save(any(Abastecimento.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private AbastecimentoRequest request() {
        AbastecimentoRequest r = new AbastecimentoRequest();
        r.setCaminhao("CAM-000001");
        r.setDtAbastecimento(QUANDO);
        r.setKmOdometro(150_000);
        r.setQtLitros(new BigDecimal("100"));
        r.setValorLitro(new BigDecimal("5.50"));
        r.setTipoCombustivel(TipoCombustivel.DIESEL_COMUM);
        r.setPosto("Posto Central");
        r.setCidade("Recife");
        r.setUf("PE");
        r.setNumNotaOuCupom("123");
        return r;
    }

    private Abastecimento salvo() {
        ArgumentCaptor<Abastecimento> c = ArgumentCaptor.forClass(Abastecimento.class);
        verify(repository).save(c.capture());
        return c.getValue();
    }

    // ---- busca de caminhão e motorista

    @Test
    void caminhaoInexistenteDaErroENaoSalvaNada() {
        AbastecimentoRequest r = request();
        r.setCaminhao("CAM-X");
        when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-X")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.criar(r));

        verifyNoInteractions(repository, notificacaoService);
    }

    @Test
    void motoristaInformadoEBuscadoEMotoristaInexistenteDaErro() {
        AbastecimentoRequest r = request();
        r.setMotorista("MOT-X");
        when(motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno("MOT-X")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.criar(r));
        verify(repository, never()).save(any());
    }

    @Test
    void semMotoristaNaoConsultaOCadastro() {
        AbastecimentoRequest vazio = request();
        vazio.setMotorista("   ");

        service.criar(request());
        service.criar(vazio);

        verifyNoInteractions(motoristaRepository);
    }

    @Test
    void guardaOMotoristaInformadoEOTitularDoCaminhaoNoMomento() {
        AbastecimentoRequest r = request();
        r.setMotorista("MOT-1");
        Motorista quemAbasteceu = new Motorista();
        when(motoristaRepository.findByMotoristaPorCodigoOuPorCodigoExterno("MOT-1")).thenReturn(Optional.of(quemAbasteceu));

        service.criar(r);

        Abastecimento a = salvo();
        assertSame(quemAbasteceu, a.getMotorista());
        assertSame(titular, a.getTitularNoPeriodo());
        assertSame(caminhao, a.getCaminhao());
    }

    // ---- valor total

    @Test
    void valorTotalInformadoEMantido() {
        AbastecimentoRequest r = request();
        r.setValorTotal(new BigDecimal("560.00"));

        service.criar(r);

        assertEquals(new BigDecimal("560.00"), salvo().getValorTotal());
    }

    @Test
    void semValorTotalCalculaLitrosVezesPreco() {
        service.criar(request());

        assertEquals(0, new BigDecimal("550.0000").compareTo(salvo().getValorTotal()));
    }

    @Test
    void semValorTotalNemDadosParaCalcularFicaNulo() {
        AbastecimentoRequest r = request();
        r.setQtLitros(null);

        service.criar(r);

        assertNull(salvo().getValorTotal());
    }

    // ---- média km/l

    @Test
    void usaAMediaCalculadaPeloSistema() {
        when(calcularMediaKmLitroService.calcular(caminhao, QUANDO, 150_000, new BigDecimal("100"))).thenReturn(new BigDecimal("3.250"));
        AbastecimentoRequest r = request();
        r.setMediaKmLitro(new BigDecimal("9.99"));

        service.criar(r);

        assertEquals(new BigDecimal("3.250"), salvo().getMediaKmLitro());
    }

    @Test
    void semMediaCalculadaUsaAInformadaNaRequisicao() {
        when(calcularMediaKmLitroService.calcular(any(), any(), any(), any())).thenReturn(null);
        AbastecimentoRequest r = request();
        r.setMediaKmLitro(new BigDecimal("2.80"));

        service.criar(r);

        assertEquals(new BigDecimal("2.80"), salvo().getMediaKmLitro());
    }

    // ---- posto e campos

    @Test
    void resolveOPostoCadastradoOuTextoLivre() {
        PostoAbastecimento posto = new PostoAbastecimento();
        AbastecimentoRequest r = request();
        r.setPostoAbastecimento("POS-000001");
        when(resolverPostoAbastecimentoService.resolver("Posto Central", "POS-000001")).thenReturn(posto);

        service.criar(r);

        Abastecimento a = salvo();
        assertSame(posto, a.getPostoAbastecimento());
        assertEquals("Posto Central", a.getPosto());
    }

    @Test
    void copiaOsCamposDaRequisicao() {
        service.criar(request());

        Abastecimento a = salvo();
        assertEquals(QUANDO, a.getDtAbastecimento());
        assertEquals(150_000, a.getKmOdometro());
        assertEquals(new BigDecimal("100"), a.getQtLitros());
        assertEquals(new BigDecimal("5.50"), a.getValorLitro());
        assertEquals(TipoCombustivel.DIESEL_COMUM, a.getTipoCombustivel());
        assertEquals("Recife", a.getCidade());
        assertEquals("PE", a.getUf());
        assertEquals("123", a.getNumNotaOuCupom());
    }

    // ---- anomalias

    @Test
    void avaliaAsAnomaliasAntesDeSalvar() {
        service.criar(request());

        InOrder ordem = Mockito.inOrder(detectarAnomaliaAbastecimentoService, repository);
        ordem.verify(detectarAnomaliaAbastecimentoService).avaliar(any(Abastecimento.class), eq(false));
        ordem.verify(repository).save(any(Abastecimento.class));
    }

    @Test
    void confirmarAvisosVaiParaAChecagem() {
        AbastecimentoRequest r = request();
        r.setConfirmarAvisos(true);

        service.criar(r);

        verify(detectarAnomaliaAbastecimentoService).avaliar(any(Abastecimento.class), eq(true));
    }

    @Test
    void seHaAvisosSemConfirmacaoNadaEGravadoENinguemEhNotificado() {
        doThrow(new AbastecimentoRequerConfirmacaoException(List.of("Odômetro repetido")))
                .when(detectarAnomaliaAbastecimentoService).avaliar(any(Abastecimento.class), eq(false));

        assertThrows(AbastecimentoRequerConfirmacaoException.class, () -> service.criar(request()));

        verify(repository, never()).save(any());
        verifyNoInteractions(notificacaoService);
    }

    // ---- notificação

    @Test
    void notificaOAbastecimentoCriadoComOCodigoDoCaminhao() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Abastecimento.class))).thenAnswer(inv -> {
            Abastecimento a = inv.getArgument(0);
            a.setId(id);
            a.setCodigo("ABA-000010");
            return a;
        });

        service.criar(request());

        verify(notificacaoService).notificar(eq(EventoNotificacao.ABASTECIMENTO_CRIADO), eq(TipoNotificacao.INFO),
                eq("Novo abastecimento criado"),
                eq("Abastecimento ABA-000010 registrado para o caminhão CAM-000001."),
                eq("ABASTECIMENTO"), eq(id), eq("ABA-000010"));
    }

    @Test
    void semCodigoGeradoReferenciaPeloId() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Abastecimento.class))).thenAnswer(inv -> {
            Abastecimento a = inv.getArgument(0);
            a.setId(id);
            return a;
        });

        service.criar(request());

        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), eq("ABASTECIMENTO"), eq(id), eq("ID-" + id));
    }
}
