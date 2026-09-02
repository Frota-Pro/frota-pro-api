package br.com.frotasPro.api.domain.integracao;

import br.com.frotasPro.api.domain.integracao.converter.IntegerListToStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
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

    /**
     * Além da sincronização automática normal (só o dia de hoje, a cada
     * intervaloMin), roda uma vez por dia nesse horário um reforço cobrindo
     * do dia 1 do mês até hoje — pega devolução/transferência lançada no
     * WinThor depois que a carga já tinha sido sincronizada em dias
     * anteriores do mês (ver CargaScheduled.reforcoMensalAutomatico).
     */
    @Column(name = "horario_reforco_mensal", nullable = false)
    private LocalTime horarioReforcoMensal = LocalTime.of(20, 0);

    @Column(name = "sync_caminhoes", nullable = false)
    private boolean syncCaminhoes = true;

    @Column(name = "sync_motoristas", nullable = false)
    private boolean syncMotoristas = true;

    @Column(name = "sync_cargas", nullable = false)
    private boolean syncCargas = true;

    @Convert(converter = IntegerListToStringConverter.class)
    @Column(name = "codigos_caminhoes")
    private List<Integer> codigosCaminhoes;

    @Convert(converter = IntegerListToStringConverter.class)
    @Column(name = "codigos_motoristas")
    private List<Integer> codigosMotoristas;

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
