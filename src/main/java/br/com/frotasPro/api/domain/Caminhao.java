package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "tb_caminhao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_caminhao_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_caminhao_renavam", columnNames = "renavam"),
                @UniqueConstraint(name = "uk_caminhao_chassi", columnNames = "chassi")
        }
)
public class Caminhao extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "codigo_externo", length = 50)
    private String codigoExterno;

    @Column(length = 255)
    private String descricao;

    @Column(length = 100)
    private String modelo;

    @Column(length = 50)
    private String cor;

    @Column(length = 100)
    private String marca;

    @Column(length = 10)
    private String placa;

    @Column(length = 20)
    private String antt;

    @Column(length = 20, unique = true)
    private String renavam;

    @Column(length = 50, unique = true)
    private String chassi;

    private BigDecimal tara;

    @Column(name = "max_peso", precision = 10, scale = 3)
    private BigDecimal maxPeso;

    @Column(name = "data_licenciamento")
    private LocalDate dataLicenciamento;

    @Column(length = 100)
    private String seguro;

    @OneToMany(mappedBy = "caminhao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Eixo> eixos = new ArrayList<>();

    @OneToMany(mappedBy = "caminhao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meta> metas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;
}
