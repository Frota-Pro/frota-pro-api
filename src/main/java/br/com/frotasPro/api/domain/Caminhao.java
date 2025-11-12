package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "tb_caminhao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_caminhao_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_caminhao_placa", columnNames = "placa"),
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

    @Column(length = 10, unique = true)
    private String placa;

    @Column(length = 20)
    private String antt;

    @Column(length = 20, unique = true)
    private String renavam;

    @Column(length = 50, unique = true)
    private String chassi;

    private Double tara;

    @Column(name = "max_peso")
    private Double maxPeso;

    @Column(name = "data_licenciamento")
    private LocalDate dataLicenciamento;

    @Column(length = 100)
    private String seguro;

//    @OneToMany(mappedBy = "caminhao", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Eixo> eixos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (placa != null) placa = placa.trim().toUpperCase();
        if (marca != null) marca = marca.trim();
        if (modelo != null) modelo = modelo.trim();
        if (codigo != null) codigo = codigo.trim();
        if (codigoExterno != null) codigoExterno = codigoExterno.trim();
    }
}
