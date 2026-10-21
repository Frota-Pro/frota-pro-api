package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.ParametroSistema;
import br.com.frotasPro.api.domain.Rota;
import br.com.frotasPro.api.domain.RoteirizacaoCidade;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.RoteirizacaoCidadeRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.meta.FechamentoMetaService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import br.com.frotasPro.api.util.AtualizarMetaCargaTransportadaService;
import br.com.frotasPro.api.util.AtualizarMetaConsumoCombustivelService;
import br.com.frotasPro.api.util.AtualizarMetaQuilometragemService;
import br.com.frotasPro.api.util.AtualizarMetaToneladaService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizarCargaServiceTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private AtualizarMetaQuilometragemService atualizarMetaQuilometragemService;
    @Mock
    private AtualizarMetaToneladaService atualizarMetaToneladaService;
    @Mock
    private AtualizarMetaCargaTransportadaService atualizarMetaCargaTransportadaService;
    @Mock
    private AtualizarMetaConsumoCombustivelService atualizarMetaConsumoCombustivelService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private IntegracaoWinThorConfigService integracaoWinThorConfigService;
    @Mock
    private ParametroSistemaService parametroSistemaService;
    @Mock
    private RoteirizacaoCidadeRepository roteirizacaoCidadeRepository;
    @Mock
    private FechamentoMetaService fechamentoMetaService;
    @InjectMocks
    private FinalizarCargaService service;

    private Carga carga;
    private Motorista motorista;
    private Caminhao caminhao;
    private ParametroSistema parametro;

    @BeforeEach
    void setUp() {
        motorista = new Motorista();
        motorista.setCodigo("MOT-000001");
        motorista.setStatus(Status.EM_ROTA);

        caminhao = new Caminhao();
        caminhao.setCodigo("CAM-000001");
        caminhao.setStatus(Status.EM_ROTA);

        carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCarga("CAR-000010");
        carga.setStatusCarga(Status.EM_ROTA);
        carga.setMotorista(motorista);
        carga.setCaminhao(caminhao);
        carga.setKmInicial(1000);
        carga.setPesoCarga(new BigDecimal("12.5"));
        carga.setDtSaida(LocalDate.of(2026, 10, 15));

        parametro = new ParametroSistema();
        lenient().when(parametroSistemaService.buscarOuPadrao()).thenReturn(parametro);
        lenient().when(cargaRepository.findByNumeroCarga("CAR-000010")).thenReturn(Optional.of(carga));
    }

    // ---- validações

    @Test
    void cargaInexistenteDaErro() {
        when(cargaRepository.findByNumeroCarga("CAR-999")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.finalizarCarga("CAR-999", 1500));
    }

    @Test
    void cargaJaFinalizadaNaoMudaNada() {
        carga.setDtChegada(LocalDate.of(2026, 10, 16));

        assertEquals("Carga já está finalizada", service.finalizarCarga("CAR-000010", 1500));

        verify(cargaRepository, never()).save(any());
        verifyNoInteractions(notificacaoService, atualizarMetaToneladaService);
    }

    @Test
    void cargaComKmFinalPreenchidoTambemContaComoFinalizada() {
        carga.setKmFinal(1400);

        assertEquals("Carga já está finalizada", service.finalizarCarga("CAR-000010", 1500));
    }

    @Test
    void kmFinalNuloZeradoOuNegativoEInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.finalizarCarga("CAR-000010", null));
        assertThrows(IllegalArgumentException.class, () -> service.finalizarCarga("CAR-000010", 0));
        assertThrows(IllegalArgumentException.class, () -> service.finalizarCarga("CAR-000010", -5));
    }

    @Test
    void kmFinalMenorQueOInicialEInvalido() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.finalizarCarga("CAR-000010", 999));

        assertTrue(e.getMessage().contains("menor que o KM inicial"));
    }

    @Test
    void kmFinalIgualAoInicialEAceito() {
        assertTrue(service.finalizarCarga("CAR-000010", 1000).startsWith("Carga finalizada com sucesso"));
    }

    @Test
    void cargaForaDeRotaNaoPodeSerFinalizada() {
        carga.setStatusCarga(Status.SINCRONIZADA);

        assertEquals("Não é possível finalizar uma carga que não está EM_ROTA",
                service.finalizarCarga("CAR-000010", 1500));

        verify(cargaRepository, never()).save(any());
    }

    // ---- fluxo de sucesso

    @Test
    void finalizaLiberaMotoristaECaminhaoEGravaOsDadosDeChegada() {
        String resposta = service.finalizarCarga("CAR-000010", 1500);

        assertTrue(resposta.startsWith("Carga finalizada com sucesso"));
        assertEquals(Status.FINALIZADA, carga.getStatusCarga());
        assertEquals(1500, carga.getKmFinal());
        assertEquals(FusoHorarioUtils.hojeBrasil(), carga.getDtChegada());
        assertNotNull(carga.getDtHoraChegada());
        assertEquals(Status.DISPONIVEL, motorista.getStatus());
        assertEquals(Status.DISPONIVEL, caminhao.getStatus());
        assertEquals(1500, caminhao.getOdometroUltimaCarga());
        verify(cargaRepository).save(carga);
    }

    @Test
    void finalizaCargaSemMotoristaSemQuebrar() {
        carga.setMotorista(null);

        service.finalizarCarga("CAR-000010", 1500);

        assertEquals(Status.FINALIZADA, carga.getStatusCarga());
        verify(atualizarMetaToneladaService).registrarTonelada(eq("CAM-000001"), eq(null), any(), any());
    }

    @Test
    void notificaAFinalizacaoComONumeroEOKmFinal() {
        service.finalizarCarga("CAR-000010", 1500);

        ArgumentCaptor<String> mensagem = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService).notificar(eq(EventoNotificacao.CARGA_FINALIZADA), eq(TipoNotificacao.SUCESSO),
                eq("Carga finalizada"), mensagem.capture(), eq("CARGA"), eq(carga.getId()), eq("CAR-000010"));
        assertTrue(mensagem.getValue().contains("CAR-000010"));
        assertTrue(mensagem.getValue().contains("KM final 1500"));
    }

    @Test
    void comIntegracaoAtivaNotificaPeloNumeroDoWinThor() {
        carga.setNumeroCargaExterno("98765");
        when(integracaoWinThorConfigService.isCargaIntegracaoAtiva()).thenReturn(true);

        service.finalizarCarga("CAR-000010", 1500);

        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), eq("CARGA"), eq(carga.getId()), eq("98765"));
    }

    // ---- metas

    @Test
    void somaACargaNasMetasPelaDataDeSaida() {
        service.finalizarCarga("CAR-000010", 1500);

        LocalDate dataDaSaida = LocalDate.of(2026, 10, 15);
        verify(atualizarMetaQuilometragemService).registrarQuilometragem("CAM-000001", "MOT-000001", 1000, 1500, dataDaSaida);
        verify(atualizarMetaToneladaService).registrarTonelada("CAM-000001", "MOT-000001", new BigDecimal("12.5"), dataDaSaida);
        verify(atualizarMetaCargaTransportadaService).registrarCarga("CAM-000001", "MOT-000001", dataDaSaida);
        verify(atualizarMetaConsumoCombustivelService).atualizar(caminhao, motorista, dataDaSaida);
    }

    @Test
    void competenciaDiferenteDaSaidaEUsadaNasMetas() {
        LocalDate setembro = LocalDate.of(2026, 9, 1);
        when(fechamentoMetaService.calcularCompetencia(carga.getDtSaida(), carga.getDtHoraSaida(), motorista)).thenReturn(setembro);

        service.finalizarCarga("CAR-000010", 1500);

        assertEquals(setembro, carga.getDtCompetencia());
        verify(atualizarMetaToneladaService).registrarTonelada("CAM-000001", "MOT-000001", new BigDecimal("12.5"), setembro);
        verify(atualizarMetaQuilometragemService).registrarQuilometragem("CAM-000001", "MOT-000001", 1000, 1500, setembro);
    }

    @Test
    void competenciaVaziaMantemADataDeSaida() {
        service.finalizarCarga("CAR-000010", 1500);

        assertNull(carga.getDtCompetencia());
    }

    @Test
    void cargaSumidaDoWinThorNaoContaNasMetasMasEFinalizada() {
        carga.setNaoEncontradaNoWinThor(true);

        service.finalizarCarga("CAR-000010", 1500);

        assertEquals(Status.FINALIZADA, carga.getStatusCarga());
        verify(cargaRepository).save(carga);
        verifyNoInteractions(atualizarMetaQuilometragemService, atualizarMetaToneladaService,
                atualizarMetaCargaTransportadaService, atualizarMetaConsumoCombustivelService);
        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), anyString(), any(), anyString());
    }

    // ---- tempo mínimo de entrega

    private void tempoMinimoLigado(int minutos) {
        parametro.setValidarTempoMinimoCarga(true);
        parametro.setTempoMinimoEntregaPadraoMinutos(minutos);
    }

    @Test
    void bloqueiaFinalizarAntesDoTempoMinimoPadrao() {
        tempoMinimoLigado(30);
        carga.setDtHoraSaida(FusoHorarioUtils.agoraBrasil().minusMinutes(10));

        BusinessException e = assertThrows(BusinessException.class, () -> service.finalizarCarga("CAR-000010", 1500));

        assertTrue(e.getMessage().contains("Tempo mínimo esperado para essa entrega: 30 minuto(s)"));
        verify(cargaRepository, never()).save(any());
        assertEquals(Status.EM_ROTA, carga.getStatusCarga());
    }

    @Test
    void liberaDepoisDeCumprirOTempoMinimo() {
        tempoMinimoLigado(30);
        carga.setDtHoraSaida(FusoHorarioUtils.agoraBrasil().minusMinutes(45));

        assertTrue(service.finalizarCarga("CAR-000010", 1500).startsWith("Carga finalizada com sucesso"));
    }

    @Test
    void tempoMinimoDaCidadeDaRotaTemPrioridadeSobreOPadrao() {
        tempoMinimoLigado(60);
        Rota rota = new Rota();
        rota.setCidadeInicio("Recife");
        carga.setRota(rota);
        carga.setDtHoraSaida(FusoHorarioUtils.agoraBrasil().minusMinutes(10));
        RoteirizacaoCidade cidade = new RoteirizacaoCidade();
        cidade.setTempoMinimoEntregaMinutos(5);
        when(roteirizacaoCidadeRepository.findByCidade("Recife")).thenReturn(Optional.of(cidade));

        // padrão exigiria 60 min, mas Recife só exige 5: passa
        assertTrue(service.finalizarCarga("CAR-000010", 1500).startsWith("Carga finalizada com sucesso"));
    }

    @Test
    void cidadeSemTempoCadastradoCaiNoPadrao() {
        tempoMinimoLigado(60);
        Rota rota = new Rota();
        rota.setCidadeInicio("Recife");
        carga.setRota(rota);
        carga.setDtHoraSaida(FusoHorarioUtils.agoraBrasil().minusMinutes(10));
        when(roteirizacaoCidadeRepository.findByCidade("Recife")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.finalizarCarga("CAR-000010", 1500));
    }

    @Test
    void tempoMinimoDesligadoNaoBloqueia() {
        parametro.setValidarTempoMinimoCarga(false);
        carga.setDtHoraSaida(LocalDateTime.now());

        assertTrue(service.finalizarCarga("CAR-000010", 1500).startsWith("Carga finalizada com sucesso"));
    }

    @Test
    void cargaAntigaSemHoraDeSaidaNaoBloqueia() {
        tempoMinimoLigado(30);
        carga.setDtHoraSaida(null);

        assertTrue(service.finalizarCarga("CAR-000010", 1500).startsWith("Carga finalizada com sucesso"));
    }

    @Test
    void tempoMinimoZeradoNaoBloqueia() {
        tempoMinimoLigado(0);
        carga.setDtHoraSaida(FusoHorarioUtils.agoraBrasil());

        assertTrue(service.finalizarCarga("CAR-000010", 1500).startsWith("Carga finalizada com sucesso"));
    }
}
