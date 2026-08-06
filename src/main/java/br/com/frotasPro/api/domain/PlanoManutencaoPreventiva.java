package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Regra de manutenção preventiva de um caminhão (ex: "troca de óleo a cada
 * 10.000km ou 6 meses"). ultimoKmExecutado/ultimaDataExecutada guardam o
 * ponto de partida pra contar o próximo vencimento — atualizados sempre que
 * uma Manutencao vinculada a este plano é concluída.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_plano_manutencao_preventiva")
public class PlanoManutencaoPreventiva extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(name = "intervalo_km")
    private Integer intervaloKm;

    @Column(name = "intervalo_dias")
    private Integer intervaloDias;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "ultimo_km_executado")
    private Integer ultimoKmExecutado;

    @Column(name = "ultima_data_executada")
    private LocalDate ultimaDataExecutada;

    /** Marca quando o alerta de vencimento já foi disparado — resetado quando uma manutenção vinculada é concluída. */
    @Column(name = "notificado_vencimento_em")
    private LocalDateTime notificadoVencimentoEm;
}
