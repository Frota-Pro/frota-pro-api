package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_meta")
public class Meta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "data_inicio")
    private LocalDate dataIncio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    private TipoMeta tipoMeta;

    @Column(name = "valor_meta", precision = 15, scale = 3)
    private BigDecimal valorMeta;

    @Column(name = "valor_realizado", precision = 15, scale = 3)
    private BigDecimal valorRealizado;

    private String unidade;

    @Enumerated(EnumType.STRING)
    private StatusMeta statusMeta;

    @Column(length = 150)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY)
    private Motorista motorista;

}
