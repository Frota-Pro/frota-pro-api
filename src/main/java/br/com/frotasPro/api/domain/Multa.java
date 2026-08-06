package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.GravidadeMulta;
import br.com.frotasPro.api.domain.enums.ResponsavelPagamentoMulta;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_multa")
public class Multa extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @Column(name = "data_infracao", nullable = false)
    private LocalDate dataInfracao;

    @Column(name = "orgao_autuador", length = 100)
    private String orgaoAutuador;

    @Column(name = "numero_ait", length = 50)
    private String numeroAit;

    @Column(name = "descricao_infracao", length = 255)
    private String descricaoInfracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravidade", length = 20)
    private GravidadeMulta gravidade;

    @Column(name = "pontos")
    private Integer pontos;

    @Column(name = "valor", precision = 12, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "data_vencimento_pagamento")
    private LocalDate dataVencimentoPagamento;

    @Column(name = "data_limite_recurso")
    private LocalDate dataLimiteRecurso;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", length = 30, nullable = false)
    private StatusPagamentoMulta statusPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsavel_pagamento", length = 20, nullable = false)
    private ResponsavelPagamentoMulta responsavelPagamento;

    @Column(length = 500)
    private String observacao;

    /** Marca quando o alerta de prazo (pagamento ou recurso) já foi disparado, pra não notificar de novo todo dia. */
    @Column(name = "notificado_prazo_em")
    private LocalDateTime notificadoPrazoEm;
}
