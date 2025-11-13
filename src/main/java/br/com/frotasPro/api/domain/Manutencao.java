package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_manutencao")
public class Manutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 150)
    private String descricao;

    private LocalDate dataInicioManutencao;
    private LocalDate dataFimManutencao;

    @Enumerated(EnumType.STRING)
    private TipoManutencao tipoManutencao;

    private List<String> itensTrocados;

    private String observacoes;

    private double valor;

    @Enumerated(EnumType.STRING)
    private StatusManutencao statusManutencao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @OneToMany(mappedBy = "manutencao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pneu> pneus = new ArrayList<>();

    @OneToOne(mappedBy = "manutencao", fetch = FetchType.LAZY)
    private ParadaCarga paradaCarga;

}
