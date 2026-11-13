package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Box de carregamento. A rota inteira (principal e complementos) usa o mesmo box. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_planejamento_box")
public class PlanejamentoBox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;
}
