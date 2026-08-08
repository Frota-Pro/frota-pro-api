package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Regras de negócio ajustáveis pelo admin (tela "Parâmetros do Sistema"),
 * sem precisar de deploy. Hoje é uma linha só (a "empresa padrão"), mesmo
 * padrão já usado em ConfiguracaoEmpresa e IntegracaoWinThorConfig.
 */
@Getter
@Setter
@Entity
@Table(
        name = "tb_parametro_sistema",
        uniqueConstraints = @UniqueConstraint(name = "uk_parametro_sistema_empresa", columnNames = "empresa_id")
)
public class ParametroSistema extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "uuid")
    private UUID empresaId;

    /** Dias antes do vencimento da CNH/documento do caminhão pra avisar. */
    @Column(name = "dias_antecedencia_vencimento_documento", nullable = false)
    private int diasAntecedenciaVencimentoDocumento = 5;

    /** Km de folga antes do km previsto de troca do pneu (plano preventivo) pra avisar. */
    @Column(name = "km_antecedencia_troca_pneu", nullable = false)
    private int kmAntecedenciaTrocaPneu = 500;

    /** Dias que uma manutenção pode ficar agendada/em andamento sem concluir antes de avisar. */
    @Column(name = "dias_manutencao_estagnada", nullable = false)
    private int diasManutencaoEstagnada = 7;

    /** Dias antes do prazo de recurso/pagamento de uma multa pra avisar. */
    @Column(name = "dias_antecedencia_prazo_multa", nullable = false)
    private int diasAntecedenciaPrazoMulta = 5;
}
