package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_ajudante",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_motorista_codigo", columnNames = "codigo"),
        }
)
public class Ajudante extends AuditoriaBase {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    @PrePersist
    @PreUpdate
    private void normalize(){
        if (codigo != null) codigo = codigo.trim();
        if (codigoExterno != null) codigoExterno = codigoExterno.trim();
        if (nome != null) nome = nome.trim();
    }
}
