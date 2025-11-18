package br.com.frotasPro.api.service;

import br.com.frotasPro.api.domain.CargaSyncJob;
import br.com.frotasPro.api.domain.enums.StatusCargaSyncJob;
import br.com.frotasPro.api.repository.CargaSyncJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CargaSyncJobService {

    private final CargaSyncJobRepository repository;

    public CargaSyncJob criarJob(UUID empresaId, LocalDate dataReferencia) {
        CargaSyncJob job = new CargaSyncJob();
        job.setEmpresaId(empresaId);
        job.setDataReferencia(dataReferencia);
        job.setStatus(StatusCargaSyncJob.PENDENTE);
        job.setCriadoEm(OffsetDateTime.now());
        job.setAtualizadoEm(OffsetDateTime.now());
        return repository.save(job);
    }

    public void marcarProcessando(UUID jobId) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusCargaSyncJob.PROCESSANDO);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }

    public void concluirJob(UUID jobId, int totalCargas) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusCargaSyncJob.CONCLUIDO);
            job.setTotalCargas(totalCargas);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }

    public void marcarErro(UUID jobId, String mensagemErro) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(StatusCargaSyncJob.ERRO);
            job.setMensagemErro(mensagemErro);
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);
        });
    }
}
