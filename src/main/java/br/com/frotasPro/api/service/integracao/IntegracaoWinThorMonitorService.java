package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.controller.integracao.dto.IntegracaoWinThorJobResponse;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import br.com.frotasPro.api.domain.integracao.CaminhaoSyncJob;
import br.com.frotasPro.api.domain.integracao.CargaSyncJob;
import br.com.frotasPro.api.domain.integracao.MotoristaSyncJob;
import br.com.frotasPro.api.integracao.dto.CaminhaoSyncRequestEvent;
import br.com.frotasPro.api.integracao.dto.CargaSyncRequestEvent;
import br.com.frotasPro.api.integracao.dto.MotoristaSyncRequestEvent;
import br.com.frotasPro.api.integracao.kafka.CaminhaoSyncRequestProducer;
import br.com.frotasPro.api.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.integracao.kafka.MotoristaSyncRequestProducer;
import br.com.frotasPro.api.repository.integracao.CaminhaoSyncJobRepository;
import br.com.frotasPro.api.repository.integracao.CargaSyncJobRepository;
import br.com.frotasPro.api.repository.integracao.MotoristaSyncJobRepository;
import br.com.frotasPro.api.service.caminhao.CaminhaoSyncJobService;
import br.com.frotasPro.api.service.carga.CargaSyncJobService;
import br.com.frotasPro.api.service.motorista.MotoristaSyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IntegracaoWinThorMonitorService {

    public enum TipoJob {
        CARGAS, CAMINHOES, MOTORISTAS, TODOS
    }

    private final CargaSyncJobRepository cargaRepo;
    private final CaminhaoSyncJobRepository caminhaoRepo;
    private final MotoristaSyncJobRepository motoristaRepo;

    private final CargaSyncRequestProducer cargaProducer;
    private final CaminhaoSyncRequestProducer caminhaoProducer;
    private final MotoristaSyncRequestProducer motoristaProducer;

    private final CargaSyncJobService cargaJobService;
    private final CaminhaoSyncJobService caminhaoJobService;
    private final MotoristaSyncJobService motoristaJobService;

    @Transactional(readOnly = true)
    public List<IntegracaoWinThorJobResponse> listarJobs(
            UUID empresaId,
            TipoJob tipo,
            Collection<StatusSincronizacao> status,
            int page,
            int size
    ) {
        int pageSize = Math.min(Math.max(size, 1), 200);
        var pageable = PageRequest.of(Math.max(page, 0), pageSize, Sort.by(Sort.Direction.DESC, "criadoEm"));

        List<IntegracaoWinThorJobResponse> out = new ArrayList<>();

        if (tipo == TipoJob.TODOS || tipo == TipoJob.CARGAS) {
            Page<CargaSyncJob> p = cargaRepo.findByEmpresaIdAndStatusInOrderByCriadoEmDesc(empresaId, status, pageable);
            p.forEach(j -> out.add(mapCarga(j)));
        }
        if (tipo == TipoJob.TODOS || tipo == TipoJob.CAMINHOES) {
            Page<CaminhaoSyncJob> p = caminhaoRepo.findByEmpresaIdAndStatusInOrderByCriadoEmDesc(empresaId, status, pageable);
            p.forEach(j -> out.add(mapCaminhao(j)));
        }
        if (tipo == TipoJob.TODOS || tipo == TipoJob.MOTORISTAS) {
            Page<MotoristaSyncJob> p = motoristaRepo.findByEmpresaIdAndStatusInOrderByCriadoEmDesc(empresaId, status, pageable);
            p.forEach(j -> out.add(mapMotorista(j)));
        }

        out.sort(Comparator.comparing(IntegracaoWinThorJobResponse::criadoEm, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return out;
    }

    @Transactional
    public void retry(UUID empresaId, TipoJob tipo, UUID jobId) {
        switch (tipo) {
            case CARGAS -> retryCarga(empresaId, jobId);
            case CAMINHOES -> retryCaminhao(empresaId, jobId);
            case MOTORISTAS -> retryMotorista(empresaId, jobId);
            default -> throw new IllegalArgumentException("Tipo de job inválido para retry: " + tipo);
        }
    }

    private void retryCarga(UUID empresaId, UUID jobId) {
        CargaSyncJob job = cargaRepo.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job de cargas não encontrado: " + jobId));
        if (!empresaId.equals(job.getEmpresaId())) throw new IllegalArgumentException("Empresa não confere para o job.");

        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setMensagemErro(null);
        job.setAtualizadoEm(OffsetDateTime.now());
        cargaRepo.save(job);

        var data = job.getDataReferencia();

        CargaSyncRequestEvent event = CargaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .dataInicial(data)
                .dataFinal(data)
                .tipoCarga("FATURADA")
                .origem("API_RETRY")
                .solicitadoPor("USUARIO")
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        cargaProducer.enviar(event);
        cargaJobService.marcarProcessando(job.getId());
    }

    private void retryCaminhao(UUID empresaId, UUID jobId) {
        CaminhaoSyncJob job = caminhaoRepo.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job de caminhões não encontrado: " + jobId));
        if (!empresaId.equals(job.getEmpresaId())) throw new IllegalArgumentException("Empresa não confere para o job.");

        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setMensagemErro(null);
        job.setAtualizadoEm(OffsetDateTime.now());
        caminhaoRepo.save(job);

        CaminhaoSyncRequestEvent event = CaminhaoSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .codFilial(null)
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        caminhaoProducer.enviar(event);
        caminhaoJobService.marcarProcessando(job.getId());
    }

    private void retryMotorista(UUID empresaId, UUID jobId) {
        MotoristaSyncJob job = motoristaRepo.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job de motoristas não encontrado: " + jobId));
        if (!empresaId.equals(job.getEmpresaId())) throw new IllegalArgumentException("Empresa não confere para o job.");

        job.setStatus(StatusSincronizacao.PENDENTE);
        job.setMensagemErro(null);
        job.setAtualizadoEm(OffsetDateTime.now());
        motoristaRepo.save(job);

        MotoristaSyncRequestEvent event = MotoristaSyncRequestEvent.builder()
                .jobId(job.getId())
                .empresaId(empresaId)
                .timestampSolicitacao(OffsetDateTime.now())
                .build();

        motoristaProducer.enviar(event);
        motoristaJobService.marcarProcessando(job.getId());
    }

    private IntegracaoWinThorJobResponse mapCarga(CargaSyncJob j) {
        return IntegracaoWinThorJobResponse.builder()
                .jobId(j.getId())
                .tipo("CARGAS")
                .status(j.getStatus())
                .dataReferencia(j.getDataReferencia())
                .totalRegistros(j.getTotalCargas())
                .mensagemErro(j.getMensagemErro())
                .criadoEm(j.getCriadoEm())
                .atualizadoEm(j.getAtualizadoEm())
                .build();
    }

    private IntegracaoWinThorJobResponse mapCaminhao(CaminhaoSyncJob j) {
        return IntegracaoWinThorJobResponse.builder()
                .jobId(j.getId())
                .tipo("CAMINHOES")
                .status(j.getStatus())
                .totalRegistros(j.getTotalCaminhoes())
                .mensagemErro(j.getMensagemErro())
                .criadoEm(j.getCriadoEm())
                .atualizadoEm(j.getAtualizadoEm())
                .build();
    }

    private IntegracaoWinThorJobResponse mapMotorista(MotoristaSyncJob j) {
        return IntegracaoWinThorJobResponse.builder()
                .jobId(j.getId())
                .tipo("MOTORISTAS")
                .status(j.getStatus())
                .totalRegistros(j.getTotalMotoristas())
                .mensagemErro(j.getMensagemErro())
                .criadoEm(j.getCriadoEm())
                .atualizadoEm(j.getAtualizadoEm())
                .build();
    }
}
