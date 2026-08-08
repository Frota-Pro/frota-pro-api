package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ordem de entrega parametrizada pra uma cidade: a sequência em que os
 * clientes daquela cidade devem ser visitados. Independe de qual rota
 * atendeu o cliente em cada carga — o cliente é da cidade, a rota pode
 * mudar. Aplicada automaticamente pelo SincronizarCargaService sempre
 * que uma carga nova chega do WinThor com clientes daquela cidade.
 */
@Getter
@Setter
@Entity
@Table(
        name = "tb_roteirizacao_cidade",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roteirizacao_cidade_cidade", columnNames = "cidade")
        }
)
public class RoteirizacaoCidade extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String cidade;

    // Mesmo formato usado em CargaNota.cliente ("codCli - nomeCli").
    @ElementCollection
    @CollectionTable(
            name = "tb_roteirizacao_cidade_cliente",
            joinColumns = @JoinColumn(name = "roteirizacao_cidade_id")
    )
    @OrderColumn(name = "ordem")
    @Column(name = "cliente", length = 200, nullable = false)
    private List<String> clientesOrdenados = new ArrayList<>();
}
