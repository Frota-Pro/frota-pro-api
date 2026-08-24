package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.service.auditoria.AuditoriaEntidadeRegistrador;
import br.com.frotasPro.api.util.AuditoriaSnapshotSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditoriaBase {

    @CreatedBy
    @Column(updatable = false)
    private String criadoPor;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedBy
    private String atualizadoPor;

    @LastModifiedDate
    private LocalDateTime atualizadoEm;

    /**
     * Foto (JSON) dos campos da entidade tirada assim que ela é carregada do
     * banco — serve de "antes" pra trilha de auditoria quando a entidade é
     * atualizada. Nunca é persistido (@Transient).
     */
    @Transient
    private String snapshotAuditoriaAnterior;

    @PostLoad
    private void capturarSnapshotAuditoria() {
        try {
            this.snapshotAuditoriaAnterior = AuditoriaSnapshotSerializer.serializar(this);
        } catch (Exception ignored) {
            // auditoria nunca pode impedir a entidade de carregar
        }
    }

    @PostPersist
    private void auditarCriacao() {
        try {
            AuditoriaEntidadeRegistrador.registrarCriacao(this);
        } catch (Exception ignored) {
            // auditoria nunca pode impedir a gravação de valer
        }
    }

    @PreUpdate
    private void auditarAtualizacao() {
        try {
            AuditoriaEntidadeRegistrador.registrarAtualizacao(this, this.snapshotAuditoriaAnterior);
        } catch (Exception ignored) {
        }
    }

    @PreRemove
    private void auditarExclusao() {
        try {
            AuditoriaEntidadeRegistrador.registrarExclusao(this);
        } catch (Exception ignored) {
        }
    }
}
