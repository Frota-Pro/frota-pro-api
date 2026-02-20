package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.StatusMeta;
import br.com.frotasPro.api.domain.enums.TipoMeta;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MetaRequest {

    @NotNull(message = "Data de início é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataIncio;

    @NotNull(message = "Data fim é obrigatória")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataFim;

    @NotNull(message = "Tipo de meta é obrigatório")
    private TipoMeta tipoMeta;

    @NotNull(message = "Valor da meta é obrigatório")
    @DecimalMin(value = "0.0001", message = "Valor da meta deve ser maior que zero")
    private BigDecimal valorMeta;

    @DecimalMin(value = "0.00", message = "Valor realizado deve ser >= 0")
    private BigDecimal valorRealizado;

    @NotBlank(message = "Unidade é obrigatória")
    @Size(max = 20, message = "Unidade deve ter no máximo 20 caracteres")
    private String unidade;

    @NotNull(message = "Status da meta é obrigatório")
    private StatusMeta statusMeta;

    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    private String descricao;

    // alvo: validação cruzada (caminhao OU categoria OU motorista) fica no service
    @Size(max = 80, message = "Caminhão inválido")
    private String caminhao;

    @Size(max = 20, message = "Categoria inválida")
    private String categoria;

    @Size(max = 80, message = "Motorista inválido")
    private String motorista;

    private Boolean renovarAutomaticamente;

    private Boolean recalcularProgresso;
}
