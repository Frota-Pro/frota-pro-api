package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.domain.integracao.CargaSyncItem;
import br.com.frotasPro.api.repository.integracao.CargaSyncItemRepository;
import br.com.frotasPro.api.repository.integracao.CargaSyncJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Guarda e consulta o histórico da sincronização de cargas (o que cada rodada fez e o que mudou em cada carga).
 * Gravar o histórico NUNCA pode atrapalhar a sincronização: roda depois que a transação do sync foi confirmada,
 * em transação própria, e qualquer falha só vira aviso no log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargaSyncHistoricoService {

    /** Por quanto tempo os itens (uma linha por carga mexida) ficam guardados. */
    public static final int DIAS_RETENCAO_ITENS = 90;

    private static final int LIMITE_HISTORICO_POR_CARGA = 200;

    private final CargaSyncItemRepository itemRepository;
    private final CargaSyncJobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /** Agenda a gravação para depois do commit do sync (ou grava na hora se não há transação, como em testes). */
    public void registrarAposCommit(HistoricoRodadaSync rodada) {
        if (rodada == null || rodada.getJobId() == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    gravarSemFalhar(rodada);
                }
            });
        } else {
            gravarSemFalhar(rodada);
        }
    }

    private void gravarSemFalhar(HistoricoRodadaSync rodada) {
        try {
            // transação NOVA e própria: roda depois do commit do sync, que já terminou
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.executeWithoutResult(status -> gravar(rodada));
        } catch (RuntimeException e) {
            log.warn("Não foi possível gravar o histórico da sincronização (jobId={}); o sync não foi afetado: {}",
                    rodada.getJobId(), e.getMessage());
        }
    }

    void gravar(HistoricoRodadaSync rodada) {
        itemRepository.saveAll(rodada.getItens());
        jobRepository.findById(rodada.getJobId()).ifPresent(job -> {
            job.setQtdNovas(rodada.getNovas());
            job.setQtdAtualizadas(rodada.getAtualizadas());
            job.setQtdSemMudanca(rodada.getSemMudanca());
            job.setQtdDescartadas(rodada.getDescartadas());
            job.setQtdDiminuicaoBloqueada(rodada.getDiminuicaoBloqueada());
            jobRepository.save(job);
        });
        log.info("Histórico da rodada: {} nova(s), {} atualizada(s), {} sem mudança, {} descartada(s), {} diminuição(ões) retida(s).",
                rodada.getNovas(), rodada.getAtualizadas(), rodada.getSemMudanca(),
                rodada.getDescartadas(), rodada.getDiminuicaoBloqueada());
    }

    @Transactional(readOnly = true)
    public List<CargaSyncItem> listarPorJob(UUID jobId) {
        return itemRepository.findByJobIdOrderByNumeroCargaExternoAsc(jobId);
    }

    @Transactional(readOnly = true)
    public List<CargaSyncItem> listarPorCarga(String numeroCargaExterno) {
        return itemRepository.findByNumeroCargaExternoOrderByCriadoEmDesc(
                numeroCargaExterno, PageRequest.of(0, LIMITE_HISTORICO_POR_CARGA));
    }

    @Transactional
    public int purgarAntigos() {
        int removidos = itemRepository.deletarAnterioresA(LocalDateTime.now().minusDays(DIAS_RETENCAO_ITENS));
        if (removidos > 0) {
            log.info("Histórico de sincronização: removidos {} item(ns) com mais de {} dias.", removidos, DIAS_RETENCAO_ITENS);
        }
        return removidos;
    }
}
