package br.com.frotasPro.api.modules.logistica.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.frotasPro.api.shared.domain.AuditoriaBase;
import br.com.frotasPro.api.shared.enums.TipoDespesa;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_despesa_parada")
public class DespesaParada extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_id", nullable = false)
    private ParadaCarga paradaCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_despesa", nullable = false, length = 30)
    private TipoDespesa tipoDespesa;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "valor", precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Column(length = 150)
    private String cidade;

    @Column(length = 2)
    private String uf;
}
