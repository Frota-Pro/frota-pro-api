package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_abastecimento",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_abastecimento_codigo", columnNames = "codigo")
        }
)
public class Abastecimento extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parada_id")
    private ParadaCarga paradaCarga;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caminhao_id", nullable = false)
    private Caminhao caminhao;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @Column(name = "dt_abastecimento", nullable = false)
    private LocalDateTime dtAbastecimento;

    @Column(name = "km_odometro")
    private Integer kmOdometro;

    @Column(name = "qt_litros", precision = 10, scale = 3)
    private BigDecimal qtLitros;

    @Column(name = "valor_litro", precision = 10, scale = 3)
    private BigDecimal valorLitro;

    @Column(name = "valor_total", precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_combustivel", nullable = false, length = 20)
    private TipoCombustivel tipoCombustivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(length = 150)
    private String posto;

    @Column(length = 150)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "numero_nota_cupom", length = 30)
    private String numNotaOuCupom;

    @Column(name = "media_km_litro", precision = 10, scale = 3)
    private BigDecimal mediaKmLitro;
}
