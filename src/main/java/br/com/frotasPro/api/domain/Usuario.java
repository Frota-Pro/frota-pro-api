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
@Table(
        name = "tb_usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_nome", columnNames = "nome"),
        }
)
public class Usuario extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Column(nullable = false, length = 50, unique = true)
    private String login;

    @Column(nullable = false, length = 150)
    private String senha;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tb_usuario_acesso",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "acesso_id"))
    private List<Acesso> acesso= new ArrayList<>();

    public void adicionarAcesso(Acesso acesso){
        this.acesso.add(acesso);
    }
}
