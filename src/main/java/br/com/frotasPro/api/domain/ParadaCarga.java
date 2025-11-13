package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.TipoParada;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_parada_carga")
public class ParadaCarga extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parada", length = 30, nullable = false)
    private TipoParada tipoParada;

    @Column(name = "dt_inicio", nullable = false)
    private LocalDateTime dtInicio;

    @Column(name = "dt_fim")
    private LocalDateTime dtFim;

    @Column(length = 150)
    private String cidade;

    @Column(length = 150)
    private String local;

    @Column(name = "km_odometro")
    private Integer kmOdometro;

    @Column(length = 500)
    private String observacao;
}
