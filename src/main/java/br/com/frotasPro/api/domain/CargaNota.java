package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tb_carga_nota")
@IdClass(CargaNotaId.class)
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
}