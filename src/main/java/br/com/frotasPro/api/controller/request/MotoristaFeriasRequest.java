package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MotoristaFeriasRequest {

    private boolean emFerias;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate feriasInicio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate feriasFimPrevisto;
}
