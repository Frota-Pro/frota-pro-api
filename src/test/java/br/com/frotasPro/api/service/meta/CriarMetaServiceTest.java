package br.com.frotasPro.api.service.meta;

import br.com.frotasPro.api.controller.request.MetaRequest;
import br.com.frotasPro.api.controller.response.MetaResponse;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.CategoriaCaminhao;
import br.com.frotasPro.api.domain.Meta;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CategoriaCaminhaoRepository;
import br.com.frotasPro.api.repository.MetaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import br.com.frotasPro.api.util.MetaProgressoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarMetaServiceTest {

    @Mock
    private MetaRepository metaRepository;
    @Mock
    private CaminhaoRepository caminhaoRepository;
    @Mock
    private CategoriaCaminhaoRepository categoriaCaminhaoRepository;
    @Mock
    private MotoristaRepository motoristaRepository;
    @Mock
    private MetaProgressoService metaProgressoService;
    @Mock
    private MetaCategoriaCaminhaoVinculoService metaCategoriaCaminhaoVinculoService;
    @InjectMocks
    private CriarMetaService service;

    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        hoje = FusoHorarioUtils.hojeBrasil();
        lenient().when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private MetaRequest request() {
        MetaRequest r = new MetaRequest();
        r.setDataIncio(hoje.withDayOfMonth(1));
        r.setDataFim(hoje.withDayOfMonth(hoje.lengthOfMonth()));
        r.setTipoMeta(TipoMeta.TONELADA);
        r.setValorMeta(new BigDecimal("100"));
        r.setUnidade("t");
        r.setStatusMeta(StatusMeta.EM_ANDAMENTO);
        return r;
    }

    private Caminhao caminhaoEncontrado(String codigo) {
        Caminhao c = new Caminhao();
        c.setCodigo(codigo);
        lenient().when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno(codigo)).thenReturn(Optional.of(c));
        return c;
    }

    // ---- validações

    @Test
    void periodoComInicioDepoisDoFimEInvalido() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        r.setDataIncio(hoje.plusDays(5));
        r.setDataFim(hoje);

        BusinessException e = assertThrows(BusinessException.class, () -> service.criar(r));

        assertTrue(e.getMessage().contains("Período inválido"));
        verify(metaRepository, never()).save(any());
    }

    @Test
    void precisaDeUmAlvo() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.criar(request()));

        assertTrue(e.getMessage().contains("Informe um alvo"));
    }

    @Test
    void alvoEmBrancoNaoContaComoAlvo() {
        MetaRequest r = request();
        r.setCaminhao("   ");

        assertThrows(BusinessException.class, () -> service.criar(r));
    }

    @Test
    void naoAceitaMaisDeUmAlvo() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        r.setMotorista("MOT-1");

        BusinessException e = assertThrows(BusinessException.class, () -> service.criar(r));

        assertTrue(e.getMessage().contains("apenas um alvo"));
        verifyNoInteractions(caminhaoRepository, motoristaRepository);
    }

    @Test
    void alvoInexistenteDaErroDeNaoEncontrado() {
        MetaRequest comCaminhao = request();
        comCaminhao.setCaminhao("CAM-X");
        when(caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-X")).thenReturn(Optional.empty());
        assertThrows(ObjectNotFound.class, () -> service.criar(comCaminhao));

        MetaRequest comCategoria = request();
        comCategoria.setCategoria("TRUCK");
        when(categoriaCaminhaoRepository.findByCodigoOuDescricao("TRUCK")).thenReturn(Optional.empty());
        assertThrows(ObjectNotFound.class, () -> service.criar(comCategoria));

        MetaRequest comMotorista = request();
        comMotorista.setMotorista("ZE");
        when(motoristaRepository.findByCodigoOuCodigoExternoOuNome("ZE")).thenReturn(Optional.empty());
        assertThrows(ObjectNotFound.class, () -> service.criar(comMotorista));
    }

    // ---- criação por tipo de alvo

    @Test
    void criaMetaParaUmCaminhaoComStatusEmAndamento() {
        MetaRequest r = request();
        r.setCaminhao("  CAM-1 ");
        Caminhao c = caminhaoEncontrado("CAM-1");
        when(metaProgressoService.calcularValorRealizado(any(Meta.class), any(), any())).thenReturn(new BigDecimal("12"));

        MetaResponse resposta = service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(c, salva.getValue().getCaminhao());
        assertEquals(StatusMeta.EM_ANDAMENTO, salva.getValue().getStatusMeta());
        assertEquals(new BigDecimal("12"), salva.getValue().getValorRealizado());
        verify(metaCategoriaCaminhaoVinculoService).sincronizar(salva.getValue());
        assertEquals(new BigDecimal("100"), resposta.getValorMeta());
    }

    @Test
    void criaMetaParaUmMotorista() {
        MetaRequest r = request();
        r.setMotorista("MOT-1");
        Motorista m = new Motorista();
        m.setCodigo("MOT-1");
        when(motoristaRepository.findByCodigoOuCodigoExternoOuNome("MOT-1")).thenReturn(Optional.of(m));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(m, salva.getValue().getMotorista());
    }

    @Test
    void criaMetaParaUmaCategoria() {
        MetaRequest r = request();
        r.setCategoria("TRUCK");
        CategoriaCaminhao cat = new CategoriaCaminhao();
        when(categoriaCaminhaoRepository.findByCodigoOuDescricao("TRUCK")).thenReturn(Optional.of(cat));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(cat, salva.getValue().getCategoria());
    }

    // ---- status por período

    @Test
    void metaQueAindaNaoComecouFicaNaoIniciada() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        r.setDataIncio(hoje.plusDays(10));
        r.setDataFim(hoje.plusDays(40));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(StatusMeta.NAO_INICIADA, salva.getValue().getStatusMeta());
    }

    @Test
    void metaDeUmPeriodoQueJaPassouNasceConcluidaESemChecarDuplicidade() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        r.setDataIncio(hoje.minusDays(60));
        r.setDataFim(hoje.minusDays(30));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(StatusMeta.CONCLUIDA, salva.getValue().getStatusMeta());
        verify(metaRepository, never()).existsMetaAtivaConflitante(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ---- duplicidade

    @Test
    void recusaMetaAtivaConflitanteDoMesmoTipoParaOMesmoAlvo() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        when(metaRepository.existsMetaAtivaConflitante(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.criar(r));

        assertTrue(e.getMessage().contains("Já existe uma meta desse tipo ativa"));
        verify(metaRepository, never()).save(any());
    }

    @Test
    void duplicidadeConsideraSoMetasAtivasEONaoIniciadas() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        when(metaRepository.existsMetaAtivaConflitante(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(false);

        service.criar(r);

        verify(metaRepository).existsMetaAtivaConflitante(TipoMeta.TONELADA,
                List.of(StatusMeta.EM_ANDAMENTO, StatusMeta.NAO_INICIADA), r.getDataIncio(), r.getDataFim(),
                caminhaoRepository.findByCaminhaoPorCodigoOuPorCodigoExterno("CAM-1").get(), null, null, null);
    }

    // ---- valor realizado

    @Test
    void valorRealizadoInformadoEMantidoSemRecalcular() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        r.setValorRealizado(new BigDecimal("33"));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(new BigDecimal("33"), salva.getValue().getValorRealizado());
        verifyNoInteractions(metaProgressoService);
    }

    @Test
    void recalcularProgressoSobrescreveOValorInformado() {
        MetaRequest r = request();
        r.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        r.setValorRealizado(new BigDecimal("33"));
        r.setRecalcularProgresso(true);
        when(metaProgressoService.calcularValorRealizado(any(Meta.class), any(), any())).thenReturn(new BigDecimal("77"));

        service.criar(r);

        ArgumentCaptor<Meta> salva = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository).save(salva.capture());
        assertEquals(new BigDecimal("77"), salva.getValue().getValorRealizado());
    }

    // ---- renovação

    @Test
    void renovarAutomaticamenteSoLigaQuandoPedidoExplicitamente() {
        MetaRequest semPedido = request();
        semPedido.setCaminhao("CAM-1");
        caminhaoEncontrado("CAM-1");
        service.criar(semPedido);

        MetaRequest comPedido = request();
        comPedido.setCaminhao("CAM-1");
        comPedido.setRenovarAutomaticamente(true);
        service.criar(comPedido);

        ArgumentCaptor<Meta> salvas = ArgumentCaptor.forClass(Meta.class);
        verify(metaRepository, org.mockito.Mockito.times(2)).save(salvas.capture());
        assertFalse(salvas.getAllValues().get(0).isRenovarAutomaticamente());
        assertTrue(salvas.getAllValues().get(1).isRenovarAutomaticamente());
    }
}
