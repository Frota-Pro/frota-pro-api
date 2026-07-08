package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.modules.integracao.domain.CargaSyncJob;
import br.com.frotasPro.api.modules.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.modules.integracao.repository.CargaSyncJobRepository;
import br.com.frotasPro.api.modules.logistica.service.CargaSyncJobService;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.StatusSincronizacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargaSyncJobServiceTest {

    @Mock
    private CargaSyncJobRepository repository;
    @Mock
    private CargaSyncRequestProducer requestProducer;
    @Mock
    private NotificacaoService notificacaoService;

    @Test
    void limitaMensagemDeErroAoTamanhoDaColuna() {
        UUID jobId = UUID.randomUUID();
        CargaSyncJob job = new CargaSyncJob();
        job.setId(jobId);
        when(repository.findById(jobId)).thenReturn(Optional.of(job));

        new CargaSyncJobService(repository, requestProducer, notificacaoService)
                .marcarErro(jobId, "x".repeat(600));

        assertEquals(StatusSincronizacao.ERRO, job.getStatus());
        assertEquals(500, job.getMensagemErro().length());
        verify(repository).save(job);
    }
}
