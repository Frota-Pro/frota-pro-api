package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_mecanico",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mecanico_codigo", columnNames = "codigo")
        }
)
public class Mecanico extends AuditoriaBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(unique = true, length = 50, nullable = false)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;
}
