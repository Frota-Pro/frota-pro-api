package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.TipoMovimentacaoPneu;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_pneu_movimentacao")
public class PneuMovimentacao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pneu_id", nullable = false)
    private Pneu pneu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoMovimentacaoPneu tipo;

    @Column(nullable = false)
    private LocalDateTime dataEvento;

    @Column(precision = 12, scale = 2)
    private BigDecimal kmEvento;

    @Column(length = 500)
    private String observacao;

    // vínculos opcionais (UUIDs)
    private UUID caminhaoId;
    private UUID manutencaoId;
    private UUID paradaId;

    private Integer eixoNumero;
    private String lado;
    private String posicao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (dataEvento == null) dataEvento = LocalDateTime.now();
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }
}
