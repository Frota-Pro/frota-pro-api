package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_configuracao_empresa",
        uniqueConstraints = @UniqueConstraint(name = "uk_configuracao_empresa_empresa", columnNames = "empresa_id")
)
public class ConfiguracaoEmpresa extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "uuid")
    private UUID empresaId;

    @Column(name = "nome_empresa", length = 150)
    private String nomeEmpresa;

    /** Logo usada no DANFE (aparece como "Emitente") e disponível pro template de e-mail. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_id")
    private Arquivo logo;

    @Column(name = "email_remetente", length = 150)
    private String emailRemetente;

    @Column(name = "email_assunto", length = 200)
    private String emailAssunto;

    /** HTML puro, com placeholders tipo {numeroNota}, {nomeEmpresa}, {cliente} substituídos no envio. */
    @Column(name = "email_corpo_html", columnDefinition = "text")
    private String emailCorpoHtml;
}
