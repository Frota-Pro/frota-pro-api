package br.com.frotasPro.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Carregamento que o operador mandou "tirar" do planejamento (ex.: venda balcão). Não volta mais. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_planejamento_removido")
public class PlanejamentoRemovido {

    @Id
    @Column(name = "numero_carregamento")
    private Integer numeroCarregamento;

    @Column(name = "removido_por", length = 200)
    private String removidoPor;

    @Column(name = "removido_em", nullable = false)
    private LocalDateTime removidoEm;
}
