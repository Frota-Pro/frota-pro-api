package br.com.frotasPro.api.integracao.kafka;

import br.com.frotasPro.api.integracao.dto.CargaSyncResponseEvent;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import br.com.frotasPro.api.service.carga.SincronizarCargaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CargaSyncResponseConsumerTest {

    @Mock
    private SincronizarCargaService cargaService;
    @Mock
    private CargaSyncJobService jobService;

    @Test
    void concluiJobDepoisDeProcessarResposta() {
        UUID jobId = UUID.randomUUID();
        CargaSyncResponseEvent event = CargaSyncResponseEvent.builder()
                .jobId(jobId)
                .totalCargas(3)
                .build();

        new CargaSyncResponseConsumer(cargaService, jobService).consumir(event);

        verify(jobService).marcarProcessando(jobId);
        verify(cargaService).sincronizarCargasWinThor(event);
        verify(jobService).concluirJob(jobId, 3);
        verify(jobService, never()).marcarErro(org.mockito.ArgumentMatchers.eq(jobId), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void registraErroQuandoProcessamentoDaRespostaFalha() {
        UUID jobId = UUID.randomUUID();
        CargaSyncResponseEvent event = CargaSyncResponseEvent.builder()
                .jobId(jobId)
                .totalCargas(1)
                .build();
        doThrow(new IllegalStateException("motorista nao encontrado")).when(cargaService)
                .sincronizarCargasWinThor(event);

        assertThrows(IllegalStateException.class,
                () -> new CargaSyncResponseConsumer(cargaService, jobService).consumir(event));

        verify(jobService).marcarProcessando(jobId);
        verify(jobService).marcarErro(org.mockito.ArgumentMatchers.eq(jobId), contains("motorista nao encontrado"));
        verify(jobService, never()).concluirJob(jobId, 1);
    }
}
