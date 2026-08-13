package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_posto_abastecimento",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_posto_abastecimento_codigo", columnNames = "codigo")
        })
public class PostoAbastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 20)
    private String cnpj;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 200)
    private String endereco;

    @Column(length = 255)
    private String observacao;

    @Column(nullable = false)
    private boolean ativo = true;
}
