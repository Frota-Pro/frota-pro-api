package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.TipoDocumentoMotorista;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_documento_motorista")
public class DocumentoMotorista extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Motorista motorista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arquivo_id", nullable = false)
    private Arquivo arquivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30, nullable = false)
    private TipoDocumentoMotorista tipoDocumento;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(length = 255)
    private String observacao;
}
