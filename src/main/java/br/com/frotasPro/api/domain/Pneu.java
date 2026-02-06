package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.StatusPneu;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_pneu")
public class Pneu {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    private String numeroSerie;
    private String marca;
    private String modelo;
    private String medida;

    @Column(nullable = false)
    private Integer nivelRecapagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPneu status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal kmMetaAtual;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal kmTotalAcumulado;

    private LocalDate dtCompra;
    private LocalDate dtDescarte;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (nivelRecapagem == null) nivelRecapagem = 0;
        if (status == null) status = StatusPneu.ESTOQUE;
        if (kmMetaAtual == null) kmMetaAtual = BigDecimal.ZERO;
        if (kmTotalAcumulado == null) kmTotalAcumulado = BigDecimal.ZERO;
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
