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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_categoria_caminhao",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categoria_caminhao_codigo", columnNames = "codigo")
        })
public class CategoriaCaminhao {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(length = 255)
    private String observacao;

    @Column(nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "categoria")
    private List<Caminhao> caminhoes = new ArrayList<>();

    // No futuro:
    // @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Meta> metas = new ArrayList<>();
}
