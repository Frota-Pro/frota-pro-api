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

    /**
     * Se true, a sincronização de cargas só sobrescreve peso/valor de uma
     * carga já existente quando o novo valor DIMINUI e existe um motivo
     * reconhecido no WinThor (devolução com código permitido, ou
     * transferência de pedido pra outro carregamento). Sem isso (false,
     * padrão), continua sobrescrevendo sempre — comportamento de antes.
     * Aumentos de peso/valor nunca são bloqueados, com ou sem essa flag.
     */
    @Column(name = "validar_motivo_alteracao_peso_valor_carga", nullable = false)
    private boolean validarMotivoAlteracaoPesoValorCarga = false;

    /**
     * Códigos de devolução (CODDEVOL, da view_devol_resumo_faturamento do
     * WinThor) que autorizam a diminuição de peso/valor, separados por
     * vírgula (ex.: "53,56"). Só é consultado quando
     * validarMotivoAlteracaoPesoValorCarga = true. Vazio/nulo = nenhum
     * código de devolução autoriza a diminuição (mais restritivo).
     */
    @Column(name = "codigos_devolucao_permitidos", length = 500)
    private String codigosDevolucaoPermitidos;

    /**
     * Se true (padrão), qualquer transferência de pedido pra outro
     * carregamento (PCLOGTRANSFNFCARREG) já autoriza a diminuição de
     * peso/valor. Se false, transferência não é aceita como motivo — só os
     * códigos de devolução permitidos. Só é consultado quando
     * validarMotivoAlteracaoPesoValorCarga = true.
     */
    @Column(name = "permitir_atualizacao_por_transferencia", nullable = false)
    private boolean permitirAtualizacaoPorTransferencia = true;
}
