package br.com.frotasPro.api.modules.logistica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_transferencia_nota")
public class CargaTransferenciaNota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transferencia_id", nullable = false)
    private CargaTransferencia transferencia;

    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Column(name = "nota", nullable = false, length = 30)
    private String nota;
}
