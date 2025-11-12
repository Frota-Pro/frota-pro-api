package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
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
@Table(name = "tb_carga")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String numeroCarga;
    private String numeroCargaExterno;

    private LocalDate dtSaida;
    private LocalDate dtPrevista;
    private LocalDate dtChegada;
    private double pesoCarga;

    private List<String> clientes = new ArrayList<>();
    private List<String> notas = new ArrayList<>();

    private Status statusCarga;

    @ManyToOne
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @OneToMany(mappedBy = "ajudante", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Ajudante> ajudantes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne
    @JoinColumn(name = "rota_id", nullable = false)
    private Rota rota;

    public long calcularAtraso() {
        if (dtPrevista == null || dtChegada == null) {
            return 0;
        }
        long diasAtraso = dtChegada.toEpochDay() - dtPrevista.toEpochDay();
        return Math.max(diasAtraso, 0);
    }

}
