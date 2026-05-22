package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_meta_resultado")
public class MetaResultado extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @Column(name = "valor_realizado", precision = 15, scale = 3)
    private BigDecimal valorRealizado;

    @Column(name = "periodo_inicio", nullable = false)
    private java.time.LocalDate periodoInicio;

    @Column(name = "periodo_fim", nullable = false)
    private java.time.LocalDate periodoFim;

    @Column(precision = 15, scale = 2)
    private BigDecimal percentual;

    @Column(name = "meta_atingida", nullable = false)
    private boolean metaAtingida;

    @Column(name = "calculado_em", nullable = false)
    private LocalDateTime calculadoEm;
}
