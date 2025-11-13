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
@Table(name = "tb_eixo")
public class Eixo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private int numero;

    @OneToMany(mappedBy = "eixo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pneu> pneus = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;
}
