package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.StatusPlanilhaPlanejamento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Planilha diária do planejamento de cargas (um dia, uma planilha). É só planejamento: a sincronização das cargas,
 * as metas e os relatórios não leem esta tabela.
 * Não estende AuditoriaBase de propósito (a ação já entra na auditoria de usuários pelo endpoint).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_planejamento_planilha")
public class PlanejamentoPlanilha {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPlanilhaPlanejamento status = StatusPlanilhaPlanejamento.ABERTA;

    @Column(name = "criada_por", length = 200)
    private String criadaPor;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "fechada_por", length = 200)
    private String fechadaPor;

    @Column(name = "fechada_em")
    private LocalDateTime fechadaEm;

    public boolean isAberta() {
        return status == StatusPlanilhaPlanejamento.ABERTA;
    }
}
