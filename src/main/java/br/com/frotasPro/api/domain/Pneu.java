package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
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
public class Pneu extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(length = 20)
    private String posicao;

    @Column(name = "data_ultima_troca")
    private LocalDate ultimaTroca;

    @Column(name = "km_ultima_troca")
    private Integer kmUltimaTroca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eixo_id", nullable = false)
    private Eixo eixo;

    @Enumerated(EnumType.STRING)
    @Column(name = "lado_atual", length = 20)
    private LadoPneu ladoAtual;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao_atual", length = 20)
    private PosicaoPneu posicaoAtual;
}
