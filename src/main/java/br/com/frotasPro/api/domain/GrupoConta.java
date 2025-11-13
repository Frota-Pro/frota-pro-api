package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", unique = true)
    private Conta conta;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", unique = true)
    private Caminhao caminhao;
}
