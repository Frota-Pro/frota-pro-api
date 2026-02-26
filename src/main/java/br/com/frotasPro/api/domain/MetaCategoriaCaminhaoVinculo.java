package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_meta_categoria_caminhao_vinculo")
public class MetaCategoriaCaminhaoVinculo extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @Column(name = "caminhao_codigo_snapshot", length = 50, nullable = false)
    private String caminhaoCodigoSnapshot;

    @Column(name = "caminhao_descricao_snapshot", length = 255)
    private String caminhaoDescricaoSnapshot;
}
