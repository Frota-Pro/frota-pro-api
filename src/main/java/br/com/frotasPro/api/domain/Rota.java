package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_rota")
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String codigo;
    private String cidadeInicio;
    private List<String> cidades;
    private int quantidadeDeDias;

    @OneToMany(mappedBy = "rota", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Carga> cargas;
}
