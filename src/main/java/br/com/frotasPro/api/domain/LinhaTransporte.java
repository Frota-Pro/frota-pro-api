package br.com.frotasPro.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Linha em que o caminhão trabalha (Frios, Linha seca...). Cadastro livre do gerente; o BI usa
 * pra separar os números. Ver {@code Caminhao.linhaTransporte}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tb_linha_transporte")
public class LinhaTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;
}
