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

    // Cidade do cliente (praça no WinThor). Não faz parte da chave — é só um
    // atributo descritivo do cliente, usado pra agrupar o histórico de
    // clientes de uma rota por cidade. Pode ser nulo (cliente sem praça
    // cadastrada no WinThor, ou nota sincronizada antes desse campo existir).
    @Column(name = "cidade", length = 150)
    private String cidade;

    /**
     * XML da NFe que originou esta nota, quando cadastrada manualmente via
     * upload (ver ImportarNotaFiscalCargaService) em vez de sincronizada do
     * WinThor. Nulo para notas vindas da integração.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arquivo_id")
    private Arquivo arquivo;

    /**
     * Vínculo com o cadastro de Cliente (CNPJ/CPF + endereço), quando já
     * identificado a partir do XML da nota. Nulo até a nota ser vista/
     * importada com o XML disponível — vai sendo preenchido aos poucos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente clienteRef;
}