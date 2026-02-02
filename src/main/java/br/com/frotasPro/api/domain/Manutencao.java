package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.domain.enums.TipoManutencao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_manutencao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_caminhao_codigo", columnNames = "codigo")
        }
)
public class Manutencao extends AuditoriaBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(length = 150)
    private String descricao;

    @Column(name = "data_inicio_manutencao", nullable = false)
    private LocalDate dataInicioManutencao;

    @Column(name = "data_fim_manutencao")
    private LocalDate dataFimManutencao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_manutencao", length = 20, nullable = false)
    private TipoManutencao tipoManutencao;

    @ElementCollection
    @CollectionTable(name = "tb_manutencao_item_trocado",
            joinColumns = @JoinColumn(name = "manutencao_id"))
    @Column(name = "item", length = 150, nullable = false)
    private List<String> itensTrocados = new ArrayList<>();

    @Column(length = 500)
    private String observacoes;

    @Column(name = "valor", precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_manutencao", length = 20, nullable = false)
    private StatusManutencao statusManutencao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id")
    private Oficina oficina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parada_id")
    private ParadaCarga paradaCarga;

    @OneToMany(mappedBy = "manutencao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrocaPneuManutencao> trocasPneu = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "manutencao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ManutencaoItem> itens = new ArrayList<>();
}
