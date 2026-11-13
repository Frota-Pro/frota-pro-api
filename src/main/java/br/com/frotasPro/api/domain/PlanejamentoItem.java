package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.DiaSaidaPlanejamento;
import br.com.frotasPro.api.domain.enums.TurnoSaidaPlanejamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Um carregamento do WinThor dentro de uma planilha de planejamento. Peso, pedidos e clientes são o retrato da última
 * leitura do WinThor. Quando é COMPLEMENTO de outro, {@code principalNumero} aponta o principal; o motorista e o
 * caminhão são da rota inteira e ficam só no principal.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_planejamento_item")
public class PlanejamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planilha_id", nullable = false)
    private PlanejamentoPlanilha planilha;

    @Column(name = "numero_carregamento", nullable = false)
    private Integer numeroCarregamento;

    @Column(name = "principal_numero")
    private Integer principalNumero;

    @Column(name = "destino", length = 200)
    private String destino;

    /** Peso em QUILOS, como vem do WinThor. */
    @Column(name = "peso_kg", nullable = false, precision = 15, scale = 3)
    private BigDecimal pesoKg = BigDecimal.ZERO;

    @Column(name = "total_pedidos", nullable = false)
    private int totalPedidos;

    @Column(name = "clientes_codigos", columnDefinition = "text")
    private String clientesCodigos;

    @Column(name = "faturada", nullable = false)
    private boolean faturada;

    @Column(name = "data_montagem")
    private LocalDate dataMontagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    /** Ajudantes da ROTA (só no principal): pode ser mais de um. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_planejamento_item_ajudante",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "ajudante_id"))
    private Set<Ajudante> ajudantes = new LinkedHashSet<>();

    /** Box da ROTA (só no principal): vale para o principal e os complementos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private PlanejamentoBox box;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_saida", length = 10)
    private DiaSaidaPlanejamento diaSaida;

    @Enumerated(EnumType.STRING)
    @Column(name = "turno_saida", length = 12)
    private TurnoSaidaPlanejamento turnoSaida;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "cod_motorista_winthor")
    private Integer codMotoristaWinthor;

    @Column(name = "cod_veiculo_winthor")
    private Integer codVeiculoWinthor;

    @Column(name = "nome_motorista_winthor", length = 150)
    private String nomeMotoristaWinthor;

    @Column(name = "placa_veiculo_winthor", length = 20)
    private String placaVeiculoWinthor;

    /** Cor da rota (índice da paleta da tela): só no principal que tem complementos. */
    @Column(name = "cor")
    private Integer cor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id")
    private Caminhao caminhao;

    public boolean isComplemento() {
        return principalNumero != null;
    }

    public Set<Integer> getCodigosClientes() {
        if (clientesCodigos == null || clientesCodigos.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(clientesCodigos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setCodigosClientes(java.util.Collection<Integer> codigos) {
        this.clientesCodigos = codigos == null || codigos.isEmpty() ? null
                : codigos.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
