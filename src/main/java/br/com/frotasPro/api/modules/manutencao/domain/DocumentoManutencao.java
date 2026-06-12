package br.com.frotasPro.api.modules.manutencao.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import br.com.frotasPro.api.modules.arquivo.domain.Arquivo;
import br.com.frotasPro.api.shared.domain.AuditoriaBase;
import br.com.frotasPro.api.shared.enums.TipoDocumentoManutencao;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_documento_manutencao")
public class DocumentoManutencao extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manutencao_id", nullable = false)
    private Manutencao manutencao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30, nullable = false)
    private TipoDocumentoManutencao tipoDocumento;

    @Column(length = 255)
    private String observacao;
}
