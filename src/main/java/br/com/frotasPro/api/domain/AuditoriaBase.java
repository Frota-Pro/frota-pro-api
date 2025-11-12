package br.com.frotasPro.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
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
}
