package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_pneu")
public class Pneu extends AuditoriaBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 20)
    private String posicao;

    @Column(name = "data_ultima_troca")
    private LocalDate ultimaTroca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eixo_id", nullable = false)
    private Eixo eixo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manutencao_id")
    private Manutencao manutencao;
}
