package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusTransferenciaCarga;
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
public class Carga extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_carga", nullable = false, length = 50, unique = true)
    private String numeroCarga;

    @Column(name = "numero_carga_externo", length = 50)
    private String numeroCargaExterno;

    @Column(name = "data_faturamento")
    private LocalDate dtFaturamento;

    @Column(name = "data_saida")
    private LocalDate dtSaida;

    @Column(name = "data_prevista")
    private LocalDate dtPrevista;

    @Column(name = "data_chegada")
    private LocalDate dtChegada;

    @Column(name = "peso_carga", precision = 15, scale = 3)
    private BigDecimal pesoCarga;

    @Column(name = "valor_total", precision = 15, scale = 3)
    private BigDecimal valorTotal;

    @Column(name = "km_inicial")
    private Integer kmInicial;

    @Column(name = "km_final")
    private Integer kmFinal;

    @Column(name = "observacao_motorista", columnDefinition = "text")
    private String observacaoMotorista;

    @Column(name = "transferencia_pendente", nullable = false)
    private boolean transferenciaPendente = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_transferencia", nullable = false, length = 30)
    private StatusTransferenciaCarga statusTransferencia = StatusTransferenciaCarga.SEM_TRANSFERENCIA;

    // A carga foi faturada pra um motorista no WinThor, mas outro foi quem
    // realmente saiu com ela (MDF-e/minuta não mudam pra refletir isso).
    // Enquanto true, o sync do WinThor não sobrescreve o motorista desta
    // carga com o que vier de lá.
    @Column(name = "motorista_definido_manualmente", nullable = false)
    private boolean motoristaDefinidoManualmente = false;

    @OneToMany(mappedBy = "carga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargaNota> notas = new ArrayList<>();

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_carga_ordem_entrega",
            joinColumns = @JoinColumn(name = "carga_id")
    )
    @OrderColumn(name = "ordem")
    @Column(name = "cliente", length = 200, nullable = false)
    private List<String> ordemEntregaClientes = new ArrayList<>();

    // Clientes desta carga que entraram sem posição parametrizada na
    // roteirização da cidade deles (RoteirizacaoCidade) no momento do sync.
    // Fica registrado nesta carga mesmo que a cidade seja roteirizada
    // depois — só a próxima carga sai com a ordem correta automaticamente.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_carga_cliente_nao_roteirizado",
            joinColumns = @JoinColumn(name = "carga_id")
    )
    @Column(name = "cliente", length = 200, nullable = false)
    private List<String> clientesNaoRoteirizados = new ArrayList<>();

    @OneToMany(mappedBy = "carga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParadaCarga> paradas = new ArrayList<>();

    public long calcularAtraso() {
        if (dtPrevista == null || dtChegada == null) return 0;
        long dias = ChronoUnit.DAYS.between(dtPrevista, dtChegada);
        return Math.max(dias, 0);
    }

    public long calcularAtrasoInicio() {
        if (dtPrevista == null || dtSaida == null) return 0;
        long dias = ChronoUnit.DAYS.between(dtPrevista, dtSaida);
        return Math.max(dias, 0);
    }

    public Integer calcularKmTotal() {
        if (kmInicial == null || kmFinal == null) return 0;
        int total = kmFinal - kmInicial;
        return Math.max(total, 0);
    }
}
