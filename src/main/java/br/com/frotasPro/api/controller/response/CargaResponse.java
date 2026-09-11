package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.config.json.BigDecimalBrasilSerializer;
import br.com.frotasPro.api.config.json.BigDecimalSemZerosSerializer;
import br.com.frotasPro.api.config.json.FlexibleBigDecimalDeserializer;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.StatusTransferenciaCarga;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CargaResponse {

    private UUID id;
    private String numeroCarga;
    private String numeroCargaExterno;
    /** Número a ser exibido ao usuário: externo se a integração estiver ativa, senão interno. */
    private String numeroCargaExibicao;

    private LocalDate dtSaida;
    private LocalDate dtPrevista;
    private LocalDate dtChegada;

    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal pesoCarga;

    @JsonSerialize(using = BigDecimalBrasilSerializer.class)
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    private BigDecimal valorTotal;

    private Integer kmInicial;
    private Integer kmFinal;
    private Integer kmTotal;

    /**
     * Km final da última carga finalizada deste mesmo caminhão (antes desta),
     * pra servir de referência ao motorista na hora de iniciar — null se o
     * caminhão nunca finalizou nenhuma carga com km registrado.
     */
    private Integer ultimoKmFinalCaminhao;

    private long diasAtraso;

    private List<ClienteCargaResponse> clientes;

    /**
     * Notas desta carga que têm o XML da NFe anexado (cadastradas na mão via
     * upload — ver ImportarNotaFiscalCargaService). Vazia pra cargas
     * totalmente sincronizadas do WinThor, que não têm arquivo por nota.
     */
    private List<NotaFiscalArquivoResponse> notasComArquivo;

    private Status statusCarga;
    private boolean transferenciaPendente;
    private StatusTransferenciaCarga statusTransferencia;

    private String codigoMotorista;
    private String nomeMotorista;

    private String codigoCaminhao;
    private String placaCaminhao;

    private String codigoRota;

    private List<String> codigosAjudantes;

    // ===== NOVOS CAMPOS =====
    /** Ordem definida para entrega (lista ordenada de clientes) */
    private List<String> ordemEntregaClientes;

    /**
     * Clientes desta carga que entraram sem posição parametrizada na
     * roteirização da cidade deles — o app mobile usa isso pra avisar o
     * motorista que a ordem daquele(s) cliente(s) não é confiável ainda.
     */
    private List<String> clientesNaoRoteirizados;

    /** Observação informada pelo motorista durante/ao final da carga */
    private String observacaoMotorista;

    /**
     * true quando o motorista desta carga foi corrigido manualmente (carga
     * faturada pra um motorista no WinThor, mas outro que realmente saiu
     * com ela) — o próximo sync do WinThor não sobrescreve.
     */
    private boolean motoristaDefinidoManualmente;

    /**
     * Códigos de devolução (CODDEVOL) encontrados no último sync com o
     * WinThor pra esta carga. Null = não foi encontrada nenhuma devolução.
     */
    private List<String> codigosDevolucaoEncontrados;

    /** true se o último sync encontrou transferência de pedido desta carga pra outro carregamento. */
    private boolean teveTransferencia;

    /**
     * true se, no último sync, uma diminuição de peso e/ou valor vinda do
     * WinThor foi ignorada por falta de motivo reconhecido (devolução com
     * código permitido, ou transferência autorizada).
     */
    private boolean diminuicaoPesoValorBloqueada;

    /** true se a última verificação de reconciliação não encontrou mais essa carga no WinThor. */
    private boolean naoEncontradaNoWinThor;

    private LocalDateTime dataVerificacaoWinThor;
}
