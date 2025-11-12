package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String login;

    @Column(nullable = false, length = 150)
    private String senha;

    @OneToMany(mappedBy = "usuario",fetch = FetchType.LAZY)
    private List<Acesso> acesso= new ArrayList<>();

    @OneToOne(mappedBy = "usuario")
    private Motorista motorista;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (login != null) login = login.trim();
        if (senha != null) senha = senha.trim();
    }
}
