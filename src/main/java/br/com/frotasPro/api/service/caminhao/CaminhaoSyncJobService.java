package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.domain.integracao.CaminhaoSyncJob;
import br.com.frotasPro.api.repository.integracao.CaminhaoSyncJobRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaminhaoSyncJobService {

    private final CaminhaoSyncJobRepository repository;
    private final NotificacaoService notificacaoService;

    public CaminhaoSyncJob criarJob(UUID empresaId) {
        CaminhaoSyncJob job = new CaminhaoSyncJob();
        job.setEmpresaId(empresaId);
        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setCriadoEm(OffsetDateTime.now());
        job.setAtualizadoEm(OffsetDateTime.now());
        CaminhaoSyncJob salvo = repository.save(job);

        notificacaoService.notificar(
                EventoNotificacao.SINCRONIZACAO_PENDENTE,
                TipoNotificacao.INFO,
                "Sincronização de caminhões pendente",
                "Job " + salvo.getId() + " criado para sincronização de caminhões.",
                "SYNC_CAMINHAO",
                salvo.getId(),
                "JOB-" + salvo.getId()
        );

        return salvo;
    }

    public void marcarProcessando(UUID jobId) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.PROCESSANDO);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }

    public void concluirJob(UUID jobId, int totalCaminhoes) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.CONCLUIDO);
            job.setTotalCaminhoes(totalCaminhoes);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);

            notificacaoService.notificar(
                    EventoNotificacao.SINCRONIZACAO_CONCLUIDA,
                    TipoNotificacao.SUCESSO,
                    "Sincronização de caminhões concluída",
                    "Job " + job.getId() + " finalizado com " + totalCaminhoes + " caminhões.",
                    "SYNC_CAMINHAO",
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
                    "Erro na sincronização de caminhões",
                    "Job " + job.getId() + " falhou: " + mensagemErro,
                    "SYNC_CAMINHAO",
                    job.getId(),
                    "JOB-" + job.getId()
            );
        });
    }
}
