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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "tb_motorista",
        //garantindo que certos campos sejam únicos
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_motorista_codigo", columnNames = "codigo"),
                @UniqueConstraint(name = "uk_motorista_cnh", columnNames = "cnh"),
                @UniqueConstraint(name = "uk_motorista_email", columnNames = "email")
        }
)
public class Motorista extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(name = "codigo_externo", length = 50)
    private String codigoExterno;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true, length = 11)
    private String cnh;

    @Column(name = "validade_cnh", nullable = false)
    private LocalDate validadeCnh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "motorista", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Carga> cargas = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (email != null) email = email.trim().toLowerCase();
        if (codigo != null) codigo = codigo.trim();
        if (codigoExterno != null) codigoExterno = codigoExterno.trim();
        if (cnh != null) cnh = cnh.replaceAll("\\D", "");
        if (nome != null) nome = nome.trim();
    }
}
