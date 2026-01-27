package br.com.frotasPro.api.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CargaNotaId implements Serializable {

    private UUID carga;     // tem que se chamar "carga" (mesmo nome do campo @Id na entidade)
    private String cliente; // tem que existir e ter o mesmo nome
    private String nota;    // tem que existir e ter o mesmo nome

    public CargaNotaId() {
    }

    public CargaNotaId(UUID carga, String cliente, String nota) {
        this.carga = carga;
        this.cliente = cliente;
        this.nota = nota;
    }

    public UUID getCarga() {
        return carga;
    }

    public String getCliente() {
        return cliente;
    }

    public String getNota() {
        return nota;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CargaNotaId that)) return false;
        return Objects.equals(carga, that.carga)
                && Objects.equals(cliente, that.cliente)
                && Objects.equals(nota, that.nota);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carga, cliente, nota);
    }
}