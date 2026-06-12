package br.com.frotasPro.api.modules.logistica.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.frotasPro.api.shared.domain.AuditoriaBase;
import br.com.frotasPro.api.shared.enums.StatusTransferenciaCarga;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_transferencia")
public class CargaTransferencia extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_origem_id", nullable = false)
    private Carga cargaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_destino_id")
    private Carga cargaDestino;

    @Column(name = "numero_carga_origem", nullable = false, length = 50)
    private String numeroCargaOrigem;

    @Column(name = "numero_carga_destino", length = 50)
    private String numeroCargaDestino;

    @Column(name = "numero_carga_externo_origem", length = 50)
    private String numeroCargaExternoOrigem;

    @Column(name = "numero_carga_externo_destino", length = 50)
    private String numeroCargaExternoDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusTransferenciaCarga status = StatusTransferenciaCarga.PENDENTE_SYNC;

    @Column(name = "total_notas")
    private Integer totalNotas;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargaTransferenciaNota> notas = new ArrayList<>();
}
