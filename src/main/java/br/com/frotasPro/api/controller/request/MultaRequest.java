package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.config.jackson.FlexibleLocalDateDeserializer;
import br.com.frotasPro.api.domain.enums.GravidadeMulta;
import br.com.frotasPro.api.domain.enums.ResponsavelPagamentoMulta;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MultaRequest {

    @NotBlank(message = "Caminhão é obrigatório")
    @Size(max = 80, message = "Caminhão inválido")
    private String caminhao;

    @Size(max = 80, message = "Motorista inválido")
    private String motorista;

    @NotNull(message = "Data da infração é obrigatória")
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate dataInfracao;

    @Size(max = 100, message = "Órgão autuador inválido")
    private String orgaoAutuador;

    @Size(max = 50, message = "Número do AIT inválido")
    private String numeroAit;

    @Size(max = 255, message = "Descrição da infração inválida")
    private String descricaoInfracao;

    private GravidadeMulta gravidade;

    private Integer pontos;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.00", message = "Valor deve ser >= 0")
    private BigDecimal valor;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate dataVencimentoPagamento;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate dataLimiteRecurso;

    @NotNull(message = "Responsável pelo pagamento é obrigatório")
    private ResponsavelPagamentoMulta responsavelPagamento;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;
}
