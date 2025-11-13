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
@Table(name = "tb_oficina")
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String nome;

    @Column(unique = true,length = 50)
    private String codigo;

    @OneToMany(mappedBy = "oficina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mecanico> mecanicos = new ArrayList<>();

    @OneToMany(mappedBy = "manutencao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Manutencao> manutencoes = new ArrayList<>();

}
