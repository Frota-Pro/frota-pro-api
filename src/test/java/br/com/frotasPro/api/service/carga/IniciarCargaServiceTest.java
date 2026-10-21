package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AjudanteRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.service.movimentacaoSemCarga.RegistrarMovimentacaoSemCargaService;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class IniciarCargaServiceTest {

    @Mock
    private CargaRepository cargaRepository;
    @Mock
    private AjudanteRepository ajudanteRepository;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private RegistrarMovimentacaoSemCargaService registrarMovimentacaoSemCargaService;
    @Mock
    private IntegracaoWinThorConfigService integracaoWinThorConfigService;
    @InjectMocks
    private IniciarCargaService service;

    private Carga carga;
    private Motorista motorista;
    private Caminhao caminhao;

    @BeforeEach
    void setUp() {
        motorista = new Motorista();
        motorista.setStatus(Status.DISPONIVEL);
        caminhao = new Caminhao();
        caminhao.setStatus(Status.DISPONIVEL);

        carga = new Carga();
        carga.setId(UUID.randomUUID());
        carga.setNumeroCarga("CAR-000020");
        carga.setStatusCarga(Status.SINCRONIZADA);
        carga.setMotorista(motorista);
        carga.setCaminhao(caminhao);

        lenient().when(cargaRepository.findByNumeroCarga("CAR-000020")).thenReturn(Optional.of(carga));
    }

    private Ajudante ajudante(String codigo) {
        Ajudante a = new Ajudante();
        a.setCodigo(codigo);
        return a;
    }

    @Test
    void cargaInexistenteDaErro() {
        when(cargaRepository.findByNumeroCarga("CAR-999")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFound.class, () -> service.iniciarCarga("CAR-999", 1000, null));
    }

    @Test
    void cargaComDataDeSaidaJaFoiIniciada() {
        carga.setDtSaida(LocalDate.of(2026, 10, 1));

        assertEquals("Carga já iniciada", service.iniciarCarga("CAR-000020", 1000, null));

        verify(cargaRepository, never()).save(any());
        verifyNoInteractions(notificacaoService, registrarMovimentacaoSemCargaService);
    }

    @Test
    void kmInicialNuloZeradoOuNegativoEInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.iniciarCarga("CAR-000020", null, null));
        assertThrows(IllegalArgumentException.class, () -> service.iniciarCarga("CAR-000020", 0, null));
        assertThrows(IllegalArgumentException.class, () -> service.iniciarCarga("CAR-000020", -1, null));
        verify(cargaRepository, never()).save(any());
    }

    @Test
    void kmInvalidoEChecadoAntesDeBuscarOsAjudantes() {
        assertThrows(IllegalArgumentException.class, () -> service.iniciarCarga("CAR-000020", 0, List.of("AJU-1")));

        verifyNoInteractions(ajudanteRepository);
    }

    @Test
    void iniciaColocandoMotoristaECaminhaoEmRotaEGravandoASaida() {
        String resposta = service.iniciarCarga("CAR-000020", 138_000, null);

        assertTrue(resposta.startsWith("Carga iniciada!"));
        assertEquals(Status.EM_ROTA, motorista.getStatus());
        assertEquals(Status.EM_ROTA, caminhao.getStatus());
        assertEquals(Status.EM_ROTA, carga.getStatusCarga());
        assertEquals(138_000, carga.getKmInicial());
        // a data de saida e o que decide em qual mes a carga conta pra meta
        assertEquals(FusoHorarioUtils.hojeBrasil(), carga.getDtSaida());
        assertNotNull(carga.getDtHoraSaida());
        verify(cargaRepository).save(carga);
    }

    @Test
    void registraAMovimentacaoSemCargaComOKmInicial() {
        service.iniciarCarga("CAR-000020", 138_000, null);

        verify(registrarMovimentacaoSemCargaService).registrarSeHouver(carga, 138_000);
    }

    @Test
    void notificaOInicioComONumeroEOKmInicial() {
        service.iniciarCarga("CAR-000020", 138_000, null);

        ArgumentCaptor<String> mensagem = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService).notificar(eq(EventoNotificacao.CARGA_INICIADA), eq(TipoNotificacao.SUCESSO),
                eq("Carga iniciada"), mensagem.capture(), eq("CARGA"), eq(carga.getId()), eq("CAR-000020"));
        assertTrue(mensagem.getValue().contains("KM inicial 138000"));
    }

    @Test
    void comIntegracaoAtivaNotificaPeloNumeroDoWinThor() {
        carga.setNumeroCargaExterno("54321");
        when(integracaoWinThorConfigService.isCargaIntegracaoAtiva()).thenReturn(true);

        service.iniciarCarga("CAR-000020", 1000, null);

        verify(notificacaoService).notificar(any(), any(), anyString(), anyString(), eq("CARGA"), eq(carga.getId()), eq("54321"));
    }

    @Test
    void vinculaOsAjudantesInformadosSubstituindoOsAnteriores() {
        carga.getAjudantes().add(ajudante("AJU-ANTIGO"));
        Ajudante a1 = ajudante("AJU-1");
        Ajudante a2 = ajudante("AJU-2");
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-1")).thenReturn(Optional.of(a1));
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-2")).thenReturn(Optional.of(a2));

        service.iniciarCarga("CAR-000020", 1000, List.of("AJU-1", "AJU-2"));

        assertEquals(List.of(a1, a2), carga.getAjudantes());
    }

    @Test
    void ajudanteInexistenteOuInativoImpedeIniciar() {
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-X")).thenReturn(Optional.empty());

        ObjectNotFound e = assertThrows(ObjectNotFound.class, () -> service.iniciarCarga("CAR-000020", 1000, List.of("AJU-X")));

        assertTrue(e.getMessage().contains("AJU-X"));
        verify(cargaRepository, never()).save(any());
        assertEquals(Status.DISPONIVEL, motorista.getStatus());
    }

    @Test
    void semAjudantesInformadosMantemOsQueJaEstavam() {
        Ajudante existente = ajudante("AJU-1");
        carga.getAjudantes().add(existente);

        service.iniciarCarga("CAR-000020", 1000, List.of());
        service.iniciarCarga("CAR-000020", 1000, null);

        assertEquals(List.of(existente), carga.getAjudantes());
        verifyNoInteractions(ajudanteRepository);
    }
}
