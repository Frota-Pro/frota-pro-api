package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.integracao.CargaSyncJob;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.repository.integracao.CargaSyncJobRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CargaSyncJobService {

    private final CargaSyncJobRepository repository;
    private final CargaSyncRequestProducer requestProducer;
    private final NotificacaoService notificacaoService;

    public CargaSyncJob criarJob(UUID empresaId, LocalDate dataReferencia) {
        CargaSyncJob job = new CargaSyncJob();
        job.setEmpresaId(empresaId);
        job.setDataReferencia(dataReferencia);
        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setCriadoEm(OffsetDateTime.now());
        job.setAtualizadoEm(OffsetDateTime.now());
        CargaSyncJob salvo = repository.save(job);

        notificacaoService.notificar(
                EventoNotificacao.SINCRONIZACAO_PENDENTE,
                TipoNotificacao.INFO,
                "Sincronização de cargas pendente",
                "Job " + salvo.getId() + " criado para a data " + dataReferencia + ".",
                "SYNC_CARGA",
                salvo.getId(),
                "JOB-" + salvo.getId()
        );

        return salvo;
    }

    public UUID solicitarSincronizacao(UUID empresaId, LocalDate dataReferencia) {
        CargaSyncJob job = criarJob(empresaId, dataReferencia);

        CargaSyncRequestEvent event = CargaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .dataInicial(dataReferencia)
                .dataFinal(dataReferencia)
                .tipoCarga("TODAS")
                .origem("API_SCHEDULER")
                .solicitadoPor("SCHEDULER")
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        requestProducer.enviar(event);

        marcarProcessando(job.getId());

        return job.getId();
    }

    public void marcarProcessando(UUID jobId) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.PROCESSANDO);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }

    public void concluirJob(UUID jobId, int totalCargas) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.CONCLUIDO);
            job.setTotalCargas(totalCargas);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);

            notificacaoService.notificar(
                    EventoNotificacao.SINCRONIZACAO_CONCLUIDA,
                    TipoNotificacao.SUCESSO,
                    "Sincronização de cargas concluída",
                    "Job " + job.getId() + " finalizado com " + totalCargas + " cargas.",
                    "SYNC_CARGA",
                    job.getId(),
                    "JOB-" + job.getId()
            );
        });
    }

    public void marcarErro(UUID jobId, String mensagemErro) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.ERRO);
            job.setMensagemErro(mensagemErro);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);

            notificacaoService.notificar(
                    EventoNotificacao.SINCRONIZACAO_ERRO,
                    TipoNotificacao.ERRO,
                    "Erro na sincronização de cargas",
                    "Job " + job.getId() + " falhou: " + mensagemErro,
                    "SYNC_CARGA",
                    job.getId(),
                    "JOB-" + job.getId()
            );
        });
    }
}
