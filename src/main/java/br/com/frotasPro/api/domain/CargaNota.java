package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_nota")
@IdClass(CargaNota.CargaNotaId.class)
public class CargaNota {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    @Id
    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Id
    @Column(name = "nota", nullable = false, length = 30)
    private String nota;

    @Getter
    @Setter
    public static class CargaNotaId implements Serializable {
        private UUID carga;
        private String cliente;
        private String nota;
    }
}
