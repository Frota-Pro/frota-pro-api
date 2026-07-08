package br.com.frotasPro.api.modules.frota.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import br.com.frotasPro.api.shared.domain.AuditoriaBase;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_eixo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_eixo_caminhao_numero",
                        columnNames = {"caminhao_id", "numero"}
                )
        }
)
public class Eixo extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private int numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;
}
