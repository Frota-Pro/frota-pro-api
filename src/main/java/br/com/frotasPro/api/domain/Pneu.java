package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_pneu")
public class Pneu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String posicao;

    private LocalDate ultimaTroca;

    @ManyToOne
    @JoinColumn(name = "eixo_id", nullable = false)
    private Eixo eixo;
}
