package br.com.frotasPro.api.service.auditoria;

import br.com.frotasPro.api.domain.LogAuditoria;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Grava um registro na trilha de auditoria. Nunca deixa uma falha aqui
 * derrubar a requisição real do usuário — na pior das hipóteses, perdemos
 * uma linha de log, o que é bem melhor do que quebrar a ação que ele
 * realmente queria fazer.
 *
 * REQUIRES_NEW é essencial aqui, não só um detalhe de isolamento: quando
 * essa gravação é disparada de dentro de um callback de ciclo de vida do
 * JPA (@PostPersist/@PreUpdate/@PreRemove em AuditoriaBase), ela acontece
 * NO MEIO do flush da entidade que está sendo auditada. Fazer um INSERT
 * na MESMA sessão/transação nesse momento mexe na fila interna do
 * Hibernate (ActionQueue) enquanto ela está sendo percorrida, e derruba o
 * flush inteiro com ConcurrentModificationException — não só a auditoria
 * falha, a operação de negócio original (iniciar carga, publicar versão
 * do app, etc.) falha junto. Com REQUIRES_NEW, a gravação do log roda numa
 * sessão/transação totalmente separada, isolada do flush em andamento.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarLogAuditoriaService {

    private final LogAuditoriaRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            String usuarioLogin,
            String usuarioNome,
            AcaoAuditoria acao,
            String entidade,
            String descricao,
            String metodoHttp,
            String endpoint,
            Integer statusHttp,
            String ip
    ) {
        registrar(usuarioLogin, usuarioNome, acao, entidade, descricao, metodoHttp, endpoint, statusHttp, ip, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            String usuarioLogin,
            String usuarioNome,
            AcaoAuditoria acao,
            String entidade,
            String descricao,
            String metodoHttp,
            String endpoint,
            Integer statusHttp,
            String ip,
            String dadosAntes,
            String dadosDepois
    ) {
        try {
            LogAuditoria log = LogAuditoria.builder()
                    .dataHora(LocalDateTime.now())
                    .usuarioLogin(usuarioLogin)
                    .usuarioNome(usuarioNome)
                    .acao(acao)
                    .entidade(entidade)
                    .descricao(descricao)
                    .metodoHttp(metodoHttp)
                    .endpoint(truncar(endpoint, 255))
                    .statusHttp(statusHttp)
                    .ip(ip)
                    .dadosAntes(dadosAntes)
                    .dadosDepois(dadosDepois)
                    .build();
            repository.save(log);
        } catch (Exception e) {
            log.error("Falha ao gravar log de auditoria (acao={}, endpoint={}): {}", acao, endpoint, e.getMessage());
        }
    }

    private String truncar(String valor, int max) {
        if (valor == null || valor.length() <= max) {
            return valor;
        }
        return valor.substring(0, max);
    }
}
