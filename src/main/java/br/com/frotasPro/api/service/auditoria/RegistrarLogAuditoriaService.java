package br.com.frotasPro.api.service.auditoria;

import br.com.frotasPro.api.domain.LogAuditoria;
import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import br.com.frotasPro.api.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Grava um registro na trilha de auditoria. Nunca deixa uma falha aqui
 * derrubar a requisição real do usuário — na pior das hipóteses, perdemos
 * uma linha de log, o que é bem melhor do que quebrar a ação que ele
 * realmente queria fazer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarLogAuditoriaService {

    private final LogAuditoriaRepository repository;

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
