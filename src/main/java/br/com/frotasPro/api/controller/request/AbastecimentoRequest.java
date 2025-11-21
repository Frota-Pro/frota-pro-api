package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AbastecimentoRequest {

    @NotNull(message = "Código é obrigatório")
    private String codigo;

    private UUID paradaId;

    @NotNull(message = "ID do caminhão é obrigatório")
    private UUID caminhaoId;

    private UUID motoristaId;

    @NotNull(message = "Data do abastecimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dtAbastecimento;

    private Integer kmOdometro;
    private BigDecimal qtLitros;
    private BigDecimal valorLitro;
    private BigDecimal valorTotal;

    @NotNull(message = "Tipo de combustível é obrigatório")
    private String tipoCombustivel;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private String formaPagamento;

    private String posto;
    private String cidade;
    private String uf;

    private String numNotaOuCupom;
}
