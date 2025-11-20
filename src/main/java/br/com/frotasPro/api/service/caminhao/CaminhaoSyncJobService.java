package br.com.frotasPro.api.service.caminhao;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.integracao.CaminhaoSyncJob;
import br.com.frotasPro.api.repository.integracao.CaminhaoSyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaminhaoSyncJobService {

    private final CaminhaoSyncJobRepository repository;

    public CaminhaoSyncJob criarJob(UUID empresaId) {
        CaminhaoSyncJob job = new CaminhaoSyncJob();
        job.setEmpresaId(empresaId);
        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setCriadoEm(OffsetDateTime.now());
        job.setAtualizadoEm(OffsetDateTime.now());
        return repository.save(job);
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
        });
    }

    public void marcarErro(UUID jobId, String mensagemErro) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.ERRO);
            job.setMensagemErro(mensagemErro);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }
}

