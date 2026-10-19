package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.MotivoCargaSyncDescartada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Carga que veio do WinThor e foi descartada no sync (ver SincronizarCargaService).
 * Sem AuditoriaBase de propósito: é um registro técnico gravado pelo próprio sync,
 * não um cadastro editado por usuário (não deve poluir o log de auditoria).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tb_carga_sync_descartada",
        uniqueConstraints = @UniqueConstraint(name = "uk_carga_sync_descartada_numcar", columnNames = "numero_carga_externo")
)
public class CargaSyncDescartada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "numero_carga_externo", nullable = false, length = 50)
    private String numeroCargaExterno;

    @Column(name = "num_mdfe")
    private Long numMdfe;

    @Column(name = "situacao_mdfe", length = 10)
    private String situacaoMdfe;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 40)
    private MotivoCargaSyncDescartada motivo;

    @Column(name = "codigo_motorista_externo", length = 50)
    private String codigoMotoristaExterno;

    @Column(name = "nome_motorista_externo", length = 200)
    private String nomeMotoristaExterno;

    @Column(name = "codigo_veiculo_externo", length = 50)
    private String codigoVeiculoExterno;

    @Column(name = "placa_veiculo_externo", length = 20)
    private String placaVeiculoExterno;

    @Column(name = "dt_saida")
    private LocalDate dtSaida;

    @Column(name = "peso_carga", precision = 14, scale = 3)
    private BigDecimal pesoCarga;

    @Column(name = "valor_total", precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "destino", length = 120)
    private String destino;

    @Column(name = "total_clientes")
    private Integer totalClientes;

    @Column(name = "tentativas", nullable = false)
    private int tentativas = 1;

    @Column(name = "primeira_ocorrencia_em", nullable = false)
    private LocalDateTime primeiraOcorrenciaEm;

    @Column(name = "ultima_ocorrencia_em", nullable = false)
    private LocalDateTime ultimaOcorrenciaEm;
}
