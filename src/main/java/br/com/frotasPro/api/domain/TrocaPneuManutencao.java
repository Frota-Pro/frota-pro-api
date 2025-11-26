package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.LadoPneu;
import br.com.frotasPro.api.domain.enums.PosicaoPneu;
import br.com.frotasPro.api.domain.enums.TipoTrocaPneu;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_troca_pneu_manutencao")
public class TrocaPneuManutencao extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manutencao_id", nullable = false)
    private Manutencao manutencao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pneu_id", nullable = false)
    private Pneu pneu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eixo_id", nullable = false)
    private Eixo eixo;

    @Enumerated(EnumType.STRING)
    @Column(name = "lado", nullable = false, length = 20)
    private LadoPneu lado;

    @Enumerated(EnumType.STRING)
    @Column(name = "posicao", nullable = false, length = 20)
    private PosicaoPneu posicao;

    @Column(name = "km_odometro", nullable = false)
    private Integer kmOdometro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_troca", nullable = false, length = 20)
    private TipoTrocaPneu tipoTroca;
}
