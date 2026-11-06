package br.com.frotasPro.api.domain.integracao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma linha do histórico de sincronização: o que uma rodada (job) fez numa carga — criou, atualizou, reteve uma
 * diminuição ou descartou — com o antes e o depois. É só registro: nenhuma regra lê esta tabela.
 * Não estende AuditoriaBase de propósito (não deve gerar linha na auditoria de usuários).
 * Peso em TONELADAS (igual a tb_carga.peso_carga); valor em R$.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_carga_sync_item")
public class CargaSyncItem {

    public static final String NOVA = "NOVA";
    public static final String ATUALIZADA = "ATUALIZADA";
    public static final String BLOQUEADA = "BLOQUEADA";
    public static final String DESCARTADA = "DESCARTADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
    private UUID jobId;

    @Column(name = "numero_carga_externo", nullable = false, length = 50)
    private String numeroCargaExterno;

    @Column(name = "carga_id", columnDefinition = "uuid")
    private UUID cargaId;

    /** NOVA, ATUALIZADA, BLOQUEADA (diminuição retida) ou DESCARTADA. */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "peso_antes", precision = 15, scale = 3)
    private BigDecimal pesoAntes;

    @Column(name = "peso_depois", precision = 15, scale = 3)
    private BigDecimal pesoDepois;

    @Column(name = "valor_antes", precision = 15, scale = 3)
    private BigDecimal valorAntes;

    @Column(name = "valor_depois", precision = 15, scale = 3)
    private BigDecimal valorDepois;

    /** O que a minuta do WinThor dizia nesta rodada (importa quando a diminuição foi retida ou a carga descartada). */
    @Column(name = "peso_winthor", precision = 15, scale = 3)
    private BigDecimal pesoWinthor;

    @Column(name = "valor_winthor", precision = 15, scale = 3)
    private BigDecimal valorWinthor;

    @Column(name = "status_antes", length = 30)
    private String statusAntes;

    @Column(name = "status_depois", length = 30)
    private String statusDepois;

    @Column(name = "motorista_antes", length = 200)
    private String motoristaAntes;

    @Column(name = "motorista_depois", length = 200)
    private String motoristaDepois;

    @Column(name = "caminhao_antes", length = 20)
    private String caminhaoAntes;

    @Column(name = "caminhao_depois", length = 20)
    private String caminhaoDepois;

    @Column(name = "dt_faturamento_antes")
    private LocalDate dtFaturamentoAntes;

    @Column(name = "dt_faturamento_depois")
    private LocalDate dtFaturamentoDepois;

    @Column(name = "notas_antes")
    private Integer notasAntes;

    @Column(name = "notas_depois")
    private Integer notasDepois;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}
