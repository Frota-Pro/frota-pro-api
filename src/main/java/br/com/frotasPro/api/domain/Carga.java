package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_carga",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_carga_numero", columnNames = "numero_carga")
        }
)
public class Carga extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_carga", nullable = false, length = 50, unique = true)
    private String numeroCarga;

    @Column(name = "numero_carga_externo", length = 50)
    private String numeroCargaExterno;

    @Column(name = "data_saida")
    private LocalDate dtSaida;

    @Column(name = "data_prevista")
    private LocalDate dtPrevista;

    @Column(name = "data_chegada")
    private LocalDate dtChegada;

    @Column(name = "peso_carga", precision = 15, scale = 3)
    private BigDecimal pesoCarga;

    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Column(name = "km_final")
    private Integer kmFinal;

    @ElementCollection
    @CollectionTable(name = "tb_carga_cliente", joinColumns = @JoinColumn(name = "carga_id"))
    @Column(name = "cliente", length = 150, nullable = false)
    private List<String> clientes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_carga_nota", joinColumns = @JoinColumn(name = "carga_id"))
    @Column(name = "nota", length = 30, nullable = false)
    private List<String> notas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status_carga", nullable = false, length = 20)
    private Status statusCarga = Status.EM_ROTA;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_carga_ajudante",
            joinColumns = @JoinColumn(name = "carga_id"),
            inverseJoinColumns = @JoinColumn(name = "ajudante_id")
    )
    private List<Ajudante> ajudantes = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rota_id", nullable = false)
    private Rota rota;

    @OneToMany(mappedBy = "carga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParadaCarga> paradas = new ArrayList<>();

    public long calcularAtraso() {
        if (dtPrevista == null || dtChegada == null) return 0;
        long dias = ChronoUnit.DAYS.between(dtPrevista, dtChegada);
        return Math.max(dias, 0);
    }

    public Integer calcularKmTotal() {
        if (kmInicial == null || kmFinal == null) return 0;
        int total = kmFinal - kmInicial;
        return Math.max(total, 0);
    }
}
