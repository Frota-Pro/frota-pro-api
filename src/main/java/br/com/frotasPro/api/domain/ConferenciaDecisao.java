package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * O que o usuário decidiu num item da tela de Conferência: confirmou (e o sistema aplicou a mudança) ou mandou
 * ignorar. É só registro: a sincronização das cargas não lê esta tabela.
 * Não estende AuditoriaBase de propósito (a ação já entra na auditoria de usuários pelo endpoint).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_conferencia_decisao")
public class ConferenciaDecisao {

    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String IGNORADA = "IGNORADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tipo", nullable = false, length = 60)
    private String tipo;

    @Column(name = "chave", nullable = false, length = 60)
    private String chave;

    @Column(name = "decisao", nullable = false, length = 20)
    private String decisao;

    @Column(name = "assinatura", length = 200)
    private String assinatura;

    @Column(name = "detalhe", length = 1000)
    private String detalhe;

    @Column(name = "decidido_por", length = 200)
    private String decididoPor;

    @Column(name = "decidido_em", nullable = false)
    private LocalDateTime decididoEm;
}
