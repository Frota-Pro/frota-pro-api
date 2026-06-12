package br.com.frotasPro.api.modules.logistica.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.frotasPro.api.modules.frota.domain.Caminhao;
import br.com.frotasPro.api.shared.domain.AuditoriaBase;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_movimentacao_sem_carga")
public class MovimentacaoSemCarga extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carga_inicio_id", nullable = false)
    private Carga cargaInicio;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "km_origem", nullable = false)
    private Integer kmOrigem;

    @Column(name = "km_destino", nullable = false)
    private Integer kmDestino;

    @Column(name = "km_rodado", nullable = false)
    private Integer kmRodado;

    @Column(name = "media_km_litro_usada", precision = 10, scale = 3)
    private BigDecimal mediaKmLitroUsada;

    @Column(name = "valor_litro_medio", precision = 10, scale = 3)
    private BigDecimal valorLitroMedio;

    @Column(name = "custo_estimado", precision = 12, scale = 2)
    private BigDecimal custoEstimado;
}
