package br.com.frotasPro.api.modules.frota.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

import br.com.frotasPro.api.modules.arquivo.domain.Arquivo;
import br.com.frotasPro.api.shared.domain.AuditoriaBase;
import br.com.frotasPro.api.shared.enums.TipoDocumentoCaminhao;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_documento_caminhao")
public class DocumentoCaminhao extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30, nullable = false)
    private TipoDocumentoCaminhao tipoDocumento;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(length = 255)
    private String observacao;
}
