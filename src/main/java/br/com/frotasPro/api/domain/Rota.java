package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_rota",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rota_codigo", columnNames = "codigo")
        }
)
public class Rota extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "cidade_inicio", length = 150, nullable = false)
    private String cidadeInicio;

    @ElementCollection
    @CollectionTable(
            name = "tb_rota_cidade",
            joinColumns = @JoinColumn(name = "rota_id")
    )
    @Column(name = "cidade", length = 150, nullable = false)
    private List<String> cidades = new ArrayList<>();

    @Column(name = "quantidade_dias")
    private int quantidadeDeDias;

    @OneToMany(mappedBy = "rota", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Carga> cargas;
}
