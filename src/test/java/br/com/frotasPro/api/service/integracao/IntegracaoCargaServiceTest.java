package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.modules.integracao.domain.CargaSyncJob;
import br.com.frotasPro.api.modules.integracao.dto.request.CargaSyncRequestEvent;
import br.com.frotasPro.api.modules.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.modules.integracao.service.IntegracaoCargaService;
import br.com.frotasPro.api.modules.integracao.service.IntegracaoWinThorConfigService;
import br.com.frotasPro.api.modules.logistica.repository.CargaRepository;
import br.com.frotasPro.api.modules.logistica.service.CargaSyncJobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegracaoCargaServiceTest {

    @Mock
    private CargaSyncJobService jobService;
    @Mock
    private CargaSyncRequestProducer producer;
    @Mock
    private IntegracaoWinThorConfigService configService;
    @Mock
    private CargaRepository cargaRepository;

    private IntegracaoCargaService service;

    @BeforeEach
    void setUp() {
        service = new IntegracaoCargaService(jobService, producer, configService, cargaRepository);
    }

    @Test
    void marcaJobComoProcessandoAposPublicarSolicitacao() {
        UUID empresaId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 5, 25);
        CargaSyncJob job = new CargaSyncJob();
        job.setId(jobId);

        when(cargaRepository.findByTransferenciaPendenteTrueAndNumeroCargaExternoIsNotNull()).thenReturn(List.of());
        when(jobService.criarJob(empresaId, data)).thenReturn(job);
        when(producer.enviar(org.mockito.ArgumentMatchers.any(CargaSyncRequestEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        UUID resultado = service.solicitarSincronizacao(
                empresaId, data, data, null, null, "FATURADA", "API_SCHEDULER", "SCHEDULER");

        ArgumentCaptor<CargaSyncRequestEvent> eventCaptor = ArgumentCaptor.forClass(CargaSyncRequestEvent.class);
        verify(producer).enviar(eventCaptor.capture());
        verify(jobService).marcarProcessando(jobId);
        assertEquals(jobId, resultado);
        assertEquals(jobId, eventCaptor.getValue().getJobId());
    }

    @Test
    void registraErroQuandoPublicacaoFalha() {
        UUID empresaId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 5, 25);
        CargaSyncJob job = new CargaSyncJob();
        job.setId(jobId);

        when(cargaRepository.findByTransferenciaPendenteTrueAndNumeroCargaExternoIsNotNull()).thenReturn(List.of());
        when(jobService.criarJob(empresaId, data)).thenReturn(job);
        doThrow(new IllegalStateException("kafka indisponivel")).when(producer)
                .enviar(org.mockito.ArgumentMatchers.any(CargaSyncRequestEvent.class));

        assertThrows(IllegalStateException.class, () -> service.solicitarSincronizacao(
                empresaId, data, data, null, null, "FATURADA", "API_SCHEDULER", "SCHEDULER"));

        verify(jobService, never()).marcarProcessando(jobId);
        verify(jobService).marcarErro(org.mockito.ArgumentMatchers.eq(jobId), contains("kafka indisponivel"));
    }

    @Test
    void registraErroQuandoKafkaFalhaAposAceitarPublicacao() {
        UUID empresaId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDate data = LocalDate.of(2026, 5, 25);
        CargaSyncJob job = new CargaSyncJob();
        job.setId(jobId);
        CompletableFuture<org.springframework.kafka.support.SendResult<String, CargaSyncRequestEvent>> envio =
                new CompletableFuture<>();

        when(cargaRepository.findByTransferenciaPendenteTrueAndNumeroCargaExternoIsNotNull()).thenReturn(List.of());
        when(jobService.criarJob(empresaId, data)).thenReturn(job);
        when(producer.enviar(org.mockito.ArgumentMatchers.any(CargaSyncRequestEvent.class))).thenReturn(envio);

        service.solicitarSincronizacao(
                empresaId, data, data, null, null, "FATURADA", "API_SCHEDULER", "SCHEDULER");
        envio.completeExceptionally(new IllegalStateException("broker indisponivel"));

        verify(jobService).marcarProcessando(jobId);
        verify(jobService).marcarErro(org.mockito.ArgumentMatchers.eq(jobId), contains("broker indisponivel"));
    }
}
