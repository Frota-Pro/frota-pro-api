package br.com.frotasPro.api.service.motorista;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.integracao.MotoristaSyncJob;
import br.com.frotasPro.api.repository.integracao.MotoristaSyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MotoristaSyncJobService {

    private final MotoristaSyncJobRepository repository;

    public MotoristaSyncJob criarJob(UUID empresaId) {
        MotoristaSyncJob job = new MotoristaSyncJob();
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

    public void concluirJob(UUID jobId, int totalMotoristas) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusSincronizacao.CONCLUIDO);
            job.setTotalMotoristas(totalMotoristas);
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

