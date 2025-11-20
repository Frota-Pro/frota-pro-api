package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "tb_grupo_conta",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_grupo_conta_codigo", columnNames = "codigo")
        }
)
public class GrupoConta extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "codigo_externo", length = 50)
    private String codigoExterno;

    @Column(nullable = false, length = 150)
    private String nome;

    @OneToMany(mappedBy = "grupoConta", cascade = CascadeType.ALL, orphanRemoval = false)
    List<Conta> contas = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", unique = true)
    private Caminhao caminhao;
}
