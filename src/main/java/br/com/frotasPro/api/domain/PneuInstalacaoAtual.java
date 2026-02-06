package br.com.frotasPro.api.domain;

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
@Table(name = "tb_pneu_instalacao_atual")
public class PneuInstalacaoAtual {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pneu_id", nullable = false, unique = true)
    private Pneu pneu;

    @Column(nullable = false)
    private UUID caminhaoId;

    @Column(nullable = false)
    private Integer eixoNumero;

    @Column(nullable = false, length = 10)
    private String lado;

    @Column(nullable = false, length = 20)
    private String posicao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal kmInstalacao;

    @Column(nullable = false)
    private LocalDateTime dataInstalacao;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (dataInstalacao == null) dataInstalacao = LocalDateTime.now();
    }
}
