package br.com.frotasPro.api.domain.integracao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "tb_integracao_winthor_config",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_integracao_winthor_config_empresa",
                columnNames = "empresa_id"
        )
)
public class IntegracaoWinThorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "uuid")
    private UUID empresaId;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "intervalo_min")
    private Integer intervaloMin;

    @Column(name = "sync_caminhoes", nullable = false)
    private boolean syncCaminhoes = true;

    @Column(name = "sync_motoristas", nullable = false)
    private boolean syncMotoristas = true;

    @Column(name = "sync_cargas", nullable = false)
    private boolean syncCargas = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.criadoEm = now;
        this.atualizadoEm = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
