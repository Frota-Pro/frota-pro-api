package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.AcaoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Trilha de auditoria: quem fez o quê e quando. Registra logins/logout e toda
 * ação que altera dados (criar/editar/excluir) em qualquer tela do sistema —
 * populado automaticamente por {@link br.com.frotasPro.api.config.LogAuditoriaFilter}
 * pra não depender de instrumentar cada service na mão. Não tem FK pro usuário
 * de propósito: o registro tem que sobreviver mesmo que o usuário seja
 * desativado/excluído depois, e login falho pode nem ter um usuário real por trás.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_log_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "usuario_login", length = 50)
    private String usuarioLogin;

    @Column(name = "usuario_nome", length = 150)
    private String usuarioNome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcaoAuditoria acao;

    @Column(length = 80)
    private String entidade;

    @Column(length = 255)
    private String descricao;

    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "status_http")
    private Integer statusHttp;

    @Column(length = 45)
    private String ip;
}
