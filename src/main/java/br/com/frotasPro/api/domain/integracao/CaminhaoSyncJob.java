package br.com.frotasPro.api.domain.integracao;

import br.com.frotasPro.api.domain.AuditoriaBase;
import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_caminhao_sync_job")
public class CaminhaoSyncJob{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSincronizacao status;

    @Column
    private Integer totalCaminhoes;

    @Column(length = 500)
    private String mensagemErro;

    @Column(nullable = false)
    private OffsetDateTime criadoEm;

    @Column(nullable = false)
    private OffsetDateTime atualizadoEm;
}
