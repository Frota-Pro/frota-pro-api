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
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CargaMinResponse {

    private String numeroCarga;
    private String numeroCargaExterno;
    /** Número a ser exibido ao usuário: externo se a integração estiver ativa, senão interno. */
    private String numeroCargaExibicao;
    private LocalDate dtSaida;
    private LocalDate dtChegada;
    @JsonSerialize(using = BigDecimalSemZerosSerializer.class)
    private BigDecimal pesoCarga;
    @JsonDeserialize(using = FlexibleBigDecimalDeserializer.class)
    @JsonSerialize(using = BigDecimalBrasilSerializer.class)
    private BigDecimal valorTotal;
    private Status statusCarga;
    private boolean transferenciaPendente;
    private StatusTransferenciaCarga statusTransferencia;
    private String nomeMotorista;
    private String placaCaminhao;

    /** Códigos de devolução (CODDEVOL) encontrados no último sync com o WinThor pra esta carga. */
    private List<String> codigosDevolucaoEncontrados;

    /** true se o último sync encontrou transferência de pedido desta carga pra outro carregamento. */
    private boolean teveTransferencia;

    /**
     * true se, no último sync, uma diminuição de peso e/ou valor vinda do
     * WinThor foi ignorada por falta de motivo reconhecido.
     */
    private boolean diminuicaoPesoValorBloqueada;
}
