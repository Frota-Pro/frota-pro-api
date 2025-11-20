package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_cliente")
@IdClass(CargaCliente.CargaClienteId.class)
public class CargaCliente {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @Id
    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Getter
    @Setter
    public static class CargaClienteId implements Serializable {
        private UUID carga;
        private String cliente;
    }
}
