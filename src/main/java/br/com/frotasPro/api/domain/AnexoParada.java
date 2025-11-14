package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.TipoAnexoParada;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_anexo_parada")
public class AnexoParada extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_id", nullable = false)
    private ParadaCarga parada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_anexo", length = 40, nullable = false)
    private TipoAnexoParada tipoAnexo;

    @Column(length = 255)
    private String observacao;
}
