package br.com.frotasPro.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Cadastro de cliente de verdade, identificado por CNPJ/CPF — fonte única
 * usada tanto pelo cadastro manual quanto pela roteirização por endereço.
 * <p>
 * Alimentado automaticamente em toda sincronização normal de carga, a partir
 * do cadastro do cliente no próprio WinThor (pcclient — ver
 * ClienteService.upsertFromWinThor / SincronizarCargaService), sem chamada
 * extra ao canal interativo. Também alimentado pelo XML da NFe: no upload
 * manual (ver ImportarNotaFiscalCargaService), no ato; ou quando alguém abre
 * o XML de uma nota específica (ver NotaFiscalService).
 */
@Getter
@Setter
@Entity
@Table(
        name = "tb_cliente",
        uniqueConstraints = @UniqueConstraint(name = "uk_cliente_documento", columnNames = "documento")
)
public class Cliente extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    /** CNPJ ou CPF, só dígitos — chave natural do cliente. */
    @Column(nullable = false, length = 20)
    private String documento;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 150)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 9)
    private String cep;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    /** codcli do WinThor, quando o cliente foi visto por lá — só referência, não é chave. */
    @Column(name = "codigo_externo", length = 30)
    private String codigoExterno;
}
