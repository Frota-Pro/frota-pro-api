package br.com.frotasPro.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Motorista confirmou que a meta do mês está ok. Uma linha por motorista/mês;
 * reabrir só preenche {@code reabertoEm} (o histórico de quando confirmou fica).
 * Sem AuditoriaBase de propósito: já guarda quem/quando por conta própria.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tb_fechamento_meta_motorista",
        uniqueConstraints = @UniqueConstraint(name = "uk_fechamento_meta_motorista_mes", columnNames = {"motorista_id", "mes_referencia"})
)
public class FechamentoMetaMotorista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    /** Sempre o dia 1 do mês fechado. */
    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Column(name = "confirmado_em", nullable = false)
    private LocalDateTime confirmadoEm;

    @Column(name = "confirmado_por", length = 100)
    private String confirmadoPor;

    /** APP (o próprio motorista) ou ADMIN. */
    @Column(name = "origem", nullable = false, length = 20)
    private String origem = "APP";

    @Column(name = "reaberto_em")
    private LocalDateTime reabertoEm;

    @Column(name = "reaberto_por", length = 100)
    private String reabertoPor;

    public boolean isAtivo() {
        return reabertoEm == null;
    }
}
