package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ParadaCargaRequest {

    @NotNull(message = "ID da carga é obrigatório")
    private UUID cargaId;

    @NotNull(message = "Tipo de parada é obrigatório")
    private String tipoParada;

    @NotNull(message = "Data e hora inicial são obrigatórias")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtInicio;


    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime dtFim;

    private String cidade;
    private String local;
    private Integer kmOdometro;
    private String observacao;

}
