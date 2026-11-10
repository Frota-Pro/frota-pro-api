package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.request.TransferirCaminhaoCargaRequest;
import br.com.frotasPro.api.controller.request.TransferirMotoristaCargaRequest;
import br.com.frotasPro.api.controller.response.ConferenciaConfirmadaResponse;
import br.com.frotasPro.api.controller.response.ConferenciaMotoristaCaminhaoResponse;
import br.com.frotasPro.api.controller.response.ConferenciaMotoristaCaminhaoResponse.Linha;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.ConferenciaDecisao;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ConferenciaDecisaoRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.service.carga.TransferirCaminhaoCargaService;
import br.com.frotasPro.api.service.carga.TransferirMotoristaCargaService;
import br.com.frotasPro.api.service.conferencia.ConferenciaWinThorClient.ReentregaMotoristaCaminhaoItem;
import br.com.frotasPro.api.service.conferencia.ConferenciaWinThorClient.Transferencia;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConferenciaMotoristaCaminhaoServiceTest {

    private ConferenciaWinThorClient client;
    private CargaRepository cargaRepository;
    private MotoristaRepository motoristaRepository;
    private CaminhaoRepository caminhaoRepository;
    private ConferenciaDecisaoRepository decisaoRepository;
    private TransferirMotoristaCargaService transferirMotorista;
    private TransferirCaminhaoCargaService transferirCaminhao;
    private UsuarioAutenticadoService usuario;
    private ConferenciaMotoristaCaminhaoService service;

    private Motorista herculano;
    private Motorista martinho;
    private Caminhao caminhao86;
    private Caminhao caminhao253;

    @BeforeEach
    void setUp() {
        client = mock(ConferenciaWinThorClient.class);
        cargaRepository = mock(CargaRepository.class);
        motoristaRepository = mock(MotoristaRepository.class);
        caminhaoRepository = mock(CaminhaoRepository.class);
        decisaoRepository = mock(ConferenciaDecisaoRepository.class);
        transferirMotorista = mock(TransferirMotoristaCargaService.class);
        transferirCaminhao = mock(TransferirCaminhaoCargaService.class);
        usuario = mock(UsuarioAutenticadoService.class);
        when(usuario.getLogin()).thenReturn("arthenyo");

        service = new ConferenciaMotoristaCaminhaoService(client, cargaRepository, motoristaRepository, caminhaoRepository,
                decisaoRepository, transferirMotorista, transferirCaminhao, usuario,
                mock(org.springframework.transaction.PlatformTransactionManager.class));

        herculano = motorista("MOT-000001", "29", "JOSE HERCULANO");
        martinho = motorista("MOT-000015", "378", "MARTINHO DE SOUSA SA");
        caminhao86 = caminhao("CAM-000011", "86", "QFF6539");
        caminhao253 = caminhao("CAM-000040", "253", "ABC1D23");

        when(motoristaRepository.findByCodigoExterno("378")).thenReturn(Optional.of(martinho));
        when(motoristaRepository.findByCodigoExterno("29")).thenReturn(Optional.of(herculano));
        when(caminhaoRepository.findByCodigoExternoAndAtivoTrue("253")).thenReturn(Optional.of(caminhao253));
        when(caminhaoRepository.findByCodigoExternoAndAtivoTrue("86")).thenReturn(Optional.of(caminhao86));
    }

    private static Motorista motorista(String codigo, String externo, String nome) {
        Motorista m = new Motorista();
        m.setCodigo(codigo);
        m.setCodigoExterno(externo);
        m.setNome(nome);
        return m;
    }

    private static Caminhao caminhao(String codigo, String externo, String placa) {
        Caminhao c = new Caminhao();
        c.setCodigo(codigo);
        c.setCodigoExterno(externo);
        c.setPlaca(placa);
        return c;
    }

    /** O caso real: carga 422812 (CAR-001178), motorista corrigido à mão (Martinho) e caminhão ainda o do manifesto (86). */
    private Carga cargaDoCasoReal() {
        Carga carga = new Carga();
        carga.setNumeroCarga("CAR-001178");
        carga.setNumeroCargaExterno("422812");
        carga.setStatusCarga(Status.SINCRONIZADA);
        carga.setMotorista(martinho);
        carga.setMotoristaDefinidoManualmente(true);
        carga.setCaminhao(caminhao86);
        when(cargaRepository.findByNumeroCargaExterno("422812")).thenReturn(Optional.of(carga));
        return carga;
    }

    private static ReentregaMotoristaCaminhaoItem itemDoCasoReal() {
        Transferencia t = new Transferencia();
        t.setNumNota(1696853L);
        t.setNumCarAnterior(422810);
        t.setNumCarAtual(422812);
        t.setDataTransferencia(LocalDateTime.of(2026, 11, 10, 8, 30, 4));
        t.setMotivo("TRANSF. DE PEDIDO ENTRE CARGAS");

        ReentregaMotoristaCaminhaoItem item = new ReentregaMotoristaCaminhaoItem();
        item.setNumCar(422812);
        item.setNumMdfe(33288L);
        item.setSituacaoMdfe("102");
        item.setDataGeracaoMdfe(LocalDateTime.of(2026, 11, 3, 16, 45, 52));
        item.setCodMotoristaMdfe(29);
        item.setNomeMotoristaMdfe("JOSE HERCULANO");
        item.setCodVeiculoMdfe("86");
        item.setPlacaMdfe("QFF6539");
        item.setCodMotoristaCarregamento(378);
        item.setNomeMotoristaCarregamento("MARTINHO");
        item.setCodVeiculoCarregamento("253");
        item.setPlacaCarregamento("ABC1D23");
        item.setMotoristaDiferente(true);
        item.setVeiculoDiferente(true);
        item.setTransferencias(List.of(t));
        return item;
    }

    // ------------------------------------------------------------------ listar

    @Test
    void casoReal_motoristaJaCorrigidoMasCaminhaoNao_propoeSoTrocarOCaminhao() {
        cargaDoCasoReal();
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        ConferenciaMotoristaCaminhaoResponse resposta = service.listar(null, null);

        assertEquals(1, resposta.getTotal());
        Linha linha = resposta.getLinhas().get(0);
        assertEquals("CAR-001178", linha.getNumeroCarga());
        assertFalse(linha.isTrocarMotorista(), "o Martinho já está na carga");
        assertTrue(linha.isTrocarCaminhao());
        assertEquals("CAM-000040", linha.getCodigoCaminhaoNovo());
        assertEquals("ABC1D23", linha.getPlacaCaminhaoNovo());
        assertEquals("QFF6539", linha.getAtualNoFrota().getPlaca());
        assertEquals(33288L, linha.getManifesto().getNumMdfe());
        assertEquals(1, linha.getTransferencias().size());
        assertTrue(linha.isPodeConfirmar());
        assertFalse(linha.isIniciada());
        assertNull(linha.getAviso());
    }

    @Test
    void cargaJaComMotoristaECaminhaoDoCarregamentoNaoAparece() {
        Carga carga = cargaDoCasoReal();
        carga.setCaminhao(caminhao253);
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        assertEquals(0, service.listar(null, null).getTotal());
    }

    @Test
    void cargaQueNaoExisteNoFrotaNaoAparece() {
        when(cargaRepository.findByNumeroCargaExterno("422812")).thenReturn(Optional.empty());
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        assertEquals(0, service.listar(null, null).getTotal());
    }

    @Test
    void ignoradoSomeMasVoltaSeOWinThorPassarAApontarOutraCoisa() {
        cargaDoCasoReal();
        ReentregaMotoristaCaminhaoItem item = itemDoCasoReal();
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(item));

        ConferenciaDecisao ignorada = new ConferenciaDecisao();
        ignorada.setChave("422812");
        ignorada.setAssinatura("M378|V253");
        when(decisaoRepository.findByTipoAndDecisaoAndChaveIn(anyString(), anyString(), any())).thenReturn(List.of(ignorada));

        assertEquals(0, service.listar(null, null).getTotal(), "mesma proposta: continua escondido");

        item.setCodVeiculoCarregamento("300"); // o carregamento mudou de veículo
        when(caminhaoRepository.findByCodigoExternoAndAtivoTrue("300")).thenReturn(Optional.of(caminhao253));
        assertEquals(1, service.listar(null, null).getTotal(), "proposta diferente: volta para a lista");
    }

    @Test
    void cadastroFaltandoImpedeConfirmarEExplicaPorque() {
        cargaDoCasoReal();
        when(caminhaoRepository.findByCodigoExternoAndAtivoTrue("253")).thenReturn(Optional.empty());
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        Linha linha = service.listar(null, null).getLinhas().get(0);

        assertFalse(linha.isPodeConfirmar());
        assertTrue(linha.getAviso().contains("253"), linha.getAviso());
    }

    @Test
    void cargaJaIniciadaApareceComAvisoDeCuidado() {
        Carga carga = cargaDoCasoReal();
        carga.setStatusCarga(Status.EM_ROTA);
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        Linha linha = service.listar(null, null).getLinhas().get(0);

        assertTrue(linha.isIniciada());
        assertTrue(linha.isPodeConfirmar(), "a decisão é da pessoa; o aviso só chama atenção");
        assertTrue(linha.getAviso().toLowerCase().contains("confira com o motorista"), linha.getAviso());
    }

    @Test
    void iniciadasVemPrimeiro() {
        cargaDoCasoReal().setStatusCarga(Status.SINCRONIZADA);

        Carga outra = new Carga();
        outra.setNumeroCarga("CAR-001100");
        outra.setNumeroCargaExterno("422100");
        outra.setStatusCarga(Status.EM_ROTA);
        outra.setMotorista(herculano);
        outra.setCaminhao(caminhao86);
        when(cargaRepository.findByNumeroCargaExterno("422100")).thenReturn(Optional.of(outra));
        ReentregaMotoristaCaminhaoItem itemOutra = itemDoCasoReal();
        itemOutra.setNumCar(422100);

        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal(), itemOutra));

        List<Linha> linhas = service.listar(null, null).getLinhas();
        assertEquals(List.of("422100", "422812"), linhas.stream().map(Linha::getNumeroCargaExterno).toList());
    }

    // ------------------------------------------------------------------ confirmar / ignorar

    @Test
    void confirmarTrocaSoOQueDifereEGuardaADecisao() {
        cargaDoCasoReal();
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        ConferenciaConfirmadaResponse resposta = service.confirmar("422812");

        verify(transferirMotorista, never()).transferir(anyString(), any(TransferirMotoristaCargaRequest.class));
        ArgumentCaptor<TransferirCaminhaoCargaRequest> pedido = ArgumentCaptor.forClass(TransferirCaminhaoCargaRequest.class);
        verify(transferirCaminhao).transferir(Mockito.eq("CAR-001178"), pedido.capture());
        assertEquals("CAM-000040", pedido.getValue().getCodigoCaminhao());

        assertEquals("CAM-000011 - QFF6539", resposta.getCaminhaoAntes());
        assertEquals("CAM-000040 - ABC1D23", resposta.getCaminhaoDepois());
        assertEquals(resposta.getMotoristaAntes(), resposta.getMotoristaDepois());

        ArgumentCaptor<ConferenciaDecisao> decisao = ArgumentCaptor.forClass(ConferenciaDecisao.class);
        verify(decisaoRepository).save(decisao.capture());
        assertEquals("CONFIRMADA", decisao.getValue().getDecisao());
        assertEquals("REENTREGA_MOTORISTA_CAMINHAO", decisao.getValue().getTipo());
        assertEquals("422812", decisao.getValue().getChave());
        assertEquals("arthenyo", decisao.getValue().getDecididoPor());
        assertNotNull(decisao.getValue().getDecididoEm());
    }

    @Test
    void confirmarTrocaMotoristaECaminhaoQuandoOsDoisDiferem() {
        Carga carga = cargaDoCasoReal();
        carga.setMotorista(herculano);
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        service.confirmar("422812");

        ArgumentCaptor<TransferirMotoristaCargaRequest> pedido = ArgumentCaptor.forClass(TransferirMotoristaCargaRequest.class);
        verify(transferirMotorista).transferir(Mockito.eq("CAR-001178"), pedido.capture());
        assertEquals("MOT-000015", pedido.getValue().getCodigoMotorista());
        verify(transferirCaminhao).transferir(Mockito.eq("CAR-001178"), any(TransferirCaminhaoCargaRequest.class));
    }

    @Test
    void confirmarSemDiferencaNoWinThorDaConflito() {
        cargaDoCasoReal();
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of());

        assertThrows(ConflictException.class, () -> service.confirmar("422812"));
        verify(transferirCaminhao, never()).transferir(anyString(), any());
        verify(decisaoRepository, never()).save(any());
    }

    @Test
    void confirmarSemPoderConfirmarNaoMexeEmNada() {
        cargaDoCasoReal();
        when(caminhaoRepository.findByCodigoExternoAndAtivoTrue("253")).thenReturn(Optional.empty());
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        assertThrows(BusinessException.class, () -> service.confirmar("422812"));
        verify(transferirCaminhao, never()).transferir(anyString(), any());
        verify(decisaoRepository, never()).save(any());
    }

    @Test
    void ignorarSoRegistraADecisaoSemMexerNaCarga() {
        Carga carga = cargaDoCasoReal();
        when(client.reentregaMotoristaCaminhao(any(), any(), any())).thenReturn(List.of(itemDoCasoReal()));

        service.ignorar("422812");

        verify(transferirCaminhao, never()).transferir(anyString(), any());
        verify(transferirMotorista, never()).transferir(anyString(), any());
        assertSame(caminhao86, carga.getCaminhao());
        ArgumentCaptor<ConferenciaDecisao> decisao = ArgumentCaptor.forClass(ConferenciaDecisao.class);
        verify(decisaoRepository).save(decisao.capture());
        assertEquals("IGNORADA", decisao.getValue().getDecisao());
        assertEquals("M378|V253", decisao.getValue().getAssinatura());
    }

    @Test
    void cargaSemNumeroDeCarregamentoNaoConfirma() {
        Carga carga = new Carga();
        carga.setNumeroCarga("CAR-000001");
        carga.setNumeroCargaExterno("abc");
        when(cargaRepository.findByNumeroCargaExterno("abc")).thenReturn(Optional.of(carga));

        assertThrows(BusinessException.class, () -> service.confirmar("abc"));
    }
}
