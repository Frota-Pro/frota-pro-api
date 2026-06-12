package br.com.frotasPro.api.modules.logistica.service;

import br.com.frotasPro.api.modules.integracao.domain.CargaSyncJob;
import br.com.frotasPro.api.modules.integracao.dto.request.CargaSyncRequestEvent;
import br.com.frotasPro.api.modules.integracao.kafka.CargaSyncRequestProducer;
import br.com.frotasPro.api.modules.integracao.repository.CargaSyncJobRepository;
import br.com.frotasPro.api.modules.notificacao.service.NotificacaoService;
import br.com.frotasPro.api.shared.enums.EventoNotificacao;
import br.com.frotasPro.api.shared.enums.StatusSincronizacao;
import br.com.frotasPro.api.shared.enums.TipoNotificacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CargaSyncJobService {

    private static final int TAMANHO_MAXIMO_MENSAGEM_ERRO = 500;

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

        try {
            var envio = requestProducer.enviar(event);
            marcarProcessando(job.getId());
            envio.whenComplete((resultado, erro) -> {
                if (erro != null) {
                    marcarErro(job.getId(), mensagemErroPublicacao(erro));
                }
            });
        } catch (RuntimeException ex) {
            marcarErro(job.getId(), mensagemErroPublicacao(ex));
            throw ex;
        }

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
            job.setMensagemErro(limitarMensagemErro(mensagemErro));
            job.setAtualizadoEm(OffsetDateTime.now());
            repository.save(job);

            notificacaoService.notificar(
                    EventoNotificacao.SINCRONIZACAO_ERRO,
                    TipoNotificacao.ERRO,
                    "Erro na sincronização de cargas",
                    "Job " + job.getId() + " falhou: " + job.getMensagemErro(),
                    "SYNC_CARGA",
                    job.getId(),
                    "JOB-" + job.getId()
            );
        });
    }

    private String limitarMensagemErro(String mensagemErro) {
        if (mensagemErro == null) {
            return null;
        }
        if (mensagemErro.length() <= TAMANHO_MAXIMO_MENSAGEM_ERRO) {
            return mensagemErro;
        }
        return mensagemErro.substring(0, TAMANHO_MAXIMO_MENSAGEM_ERRO);
    }

    private String mensagemErroPublicacao(Throwable erro) {
        String detalhe = erro.getMessage();
        if (detalhe == null || detalhe.isBlank()) {
            return "Falha ao publicar pedido de sincronizacao de cargas.";
        }
        return "Falha ao publicar pedido de sincronizacao de cargas: " + detalhe;
    }
}
