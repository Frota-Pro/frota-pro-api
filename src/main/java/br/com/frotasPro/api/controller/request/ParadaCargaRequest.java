package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.TipoParada;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ParadaCargaRequest {

    @NotBlank(message = "Código da carga é obrigatório")
    @Size(max = 80, message = "Carga inválida")
    private String carga;

    @NotNull(message = "Tipo de parada é obrigatório")
    private TipoParada tipoParada;

    @NotNull(message = "Data e hora inicial são obrigatórias")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtInicio;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtFim;

    @Size(max = 120, message = "Cidade deve ter no máximo 120 caracteres")
    private String cidade;

    @Size(max = 150, message = "Local deve ter no máximo 150 caracteres")
    private String local;

    @PositiveOrZero(message = "KM do odômetro deve ser >= 0")
    private Integer kmOdometro;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    @DecimalMin(value = "0.01", message = "Valor da despesa deve ser maior que zero")
    private BigDecimal valorDespesa;

    @Size(max = 255, message = "Descrição da despesa deve ter no máximo 255 caracteres")
    private String descricaoDespesa;

    @Valid
    private AbastecimentoParadaRequest abastecimento;

    @Valid
    private ManutencaoParadaRequest manutencao;
}
