package br.com.frotasPro.api.domain.integracao;

import br.com.frotasPro.api.domain.enums.StatusSincronizacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_sync_job")
public class CargaSyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "uuid")
    private UUID empresaId;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusSincronizacao status = StatusSincronizacao.PENDENTE;

    @Column(name = "total_cargas")
    private Integer totalCargas;

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    /** API_SCHEDULER (automático), API_FROTAPRO (manual) ou API_RETRY (reprocessamento). */
    @Column(name = "origem", length = 30)
    private String origem;

    /** Detalha o origem: SCHEDULER, SCHEDULER_REFORCO_MENSAL, sistema ou USUARIO. */
    @Column(name = "solicitado_por", length = 50)
    private String solicitadoPor;

    @Column(name = "criado_em")
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;
}
