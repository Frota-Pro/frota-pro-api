package br.com.frotasPro.api.controller.request;

import br.com.frotasPro.api.domain.enums.TipoParada;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ParadaCargaRequest {

    @NotNull(message = "Codigo da carga é obrigatório")
    private String carga;

    @NotNull(message = "Tipo de parada é obrigatório")
    private TipoParada tipoParada;

    @NotNull(message = "Data e hora inicial são obrigatórias")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtInicio;


    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtFim;

    private String cidade;
    private String local;
    private Integer kmOdometro;
    private String observacao;

    @DecimalMin(value = "0.01", message = "Valor da despesa deve ser maior que zero")
    private BigDecimal valorDespesa;

    private String descricaoDespesa;

    private AbastecimentoParadaRequest abastecimento;

}
