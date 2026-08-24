package br.com.frotasPro.api.service.auditoria;

import br.com.frotasPro.api.repository.LogAuditoriaRepository;
import br.com.frotasPro.api.service.parametrosistema.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Apaga da trilha de auditoria (tb_log_auditoria) tudo que passou do prazo
 * de retenção configurado em Parâmetros do Sistema (padrão 180 dias) — sem
 * isso a tabela cresce sem limite, já que toda ação de escrita do sistema
 * (e login/logout) gera uma linha. Chamado diariamente por
 * {@link br.com.frotasPro.api.scheduler.AuditoriaScheduler}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurgarLogAuditoriaService {

    private final LogAuditoriaRepository repository;
    private final ParametroSistemaService parametroSistemaService;

    public void purgarAntigos() {
        int diasRetencao = parametroSistemaService.buscarOuPadrao().getDiasRetencaoAuditoria();
        if (diasRetencao <= 0) {
            diasRetencao = 180;
        }

        LocalDateTime corte = LocalDateTime.now().minusDays(diasRetencao);
        int removidos = repository.deletarAnterioresA(corte);

        if (removidos > 0) {
            log.info("Auditoria: removidos {} registro(s) anteriores a {} (retenção de {} dias).", removidos, corte, diasRetencao);
        }
    }
}
