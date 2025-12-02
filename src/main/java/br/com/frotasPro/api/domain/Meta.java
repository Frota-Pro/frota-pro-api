package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_meta")
public class Meta extends AuditoriaBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "data_inicio")
    private LocalDate dataIncio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_meta", length = 30, nullable = false)
    private TipoMeta tipoMeta;

    @Column(name = "valor_meta", precision = 15, scale = 3)
    private BigDecimal valorMeta;

    @Column(name = "valor_realizado", precision = 15, scale = 3)
    private BigDecimal valorRealizado;

    @Column(length = 50)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_meta", length = 20, nullable = false)
    private StatusMeta statusMeta;

    @Column(length = 150)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caminhao_id")
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_caminhao_id")
    private CategoriaCaminhao categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @Column(nullable = false)
    private boolean renovarAutomaticamente = false;
}
