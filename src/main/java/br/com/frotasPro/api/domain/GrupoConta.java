package br.com.frotasPro.api.domain;

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
@Table(name = "tb_grupo_conta")
public class GrupoConta {

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

    @OneToOne(mappedBy = "grupoConta")
    private Conta conta;

    @OneToOne(mappedBy = "grupoConta")
    private Caminhao caminhao;
}
