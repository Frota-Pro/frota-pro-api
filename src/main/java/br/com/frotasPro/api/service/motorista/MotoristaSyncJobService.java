package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.domain.integracao.MotoristaSyncJob;
import br.com.frotasPro.api.repository.integracao.MotoristaSyncJobRepository;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MotoristaSyncJobService {

    private final MotoristaSyncJobRepository repository;
    private final NotificacaoService notificacaoService;

    public MotoristaSyncJob criarJob(UUID empresaId) {
        MotoristaSyncJob job = new MotoristaSyncJob();
        job.setEmpresaId(empresaId);
        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setCriadoEm(OffsetDateTime.now());
        job.setAtualizadoEm(OffsetDateTime.now());
        MotoristaSyncJob salvo = repository.save(job);

        notificacaoService.notificar(
                EventoNotificacao.SINCRONIZACAO_PENDENTE,
                TipoNotificacao.INFO,
                "Sincronização de motoristas pendente",
                "Job " + salvo.getId() + " criado para sincronização de motoristas.",
                "SYNC_MOTORISTA",
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

    public void concluirJob(UUID jobId, int totalMotoristas) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.CONCLUIDO);
            job.setTotalMotoristas(totalMotoristas);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);

            notificacaoService.notificar(
                    EventoNotificacao.SINCRONIZACAO_CONCLUIDA,
                    TipoNotificacao.SUCESSO,
                    "Sincronização de motoristas concluída",
                    "Job " + job.getId() + " finalizado com " + totalMotoristas + " motoristas.",
                    "SYNC_MOTORISTA",
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
                    "Erro na sincronização de motoristas",
                    "Job " + job.getId() + " falhou: " + mensagemErro,
                    "SYNC_MOTORISTA",
                    job.getId(),
                    "JOB-" + job.getId()
            );
        });
    }
}
