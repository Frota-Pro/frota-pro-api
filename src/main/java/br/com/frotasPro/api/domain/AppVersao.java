package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_app_versao")
public class AppVersao extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "versao_nome", nullable = false, length = 30)
    private String versaoNome;

    @Column(name = "notas", columnDefinition = "text")
    private String notas;

    @Column(name = "obrigatoria", nullable = false)
    private boolean obrigatoria;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;
}
