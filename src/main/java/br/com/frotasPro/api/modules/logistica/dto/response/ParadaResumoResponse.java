package br.com.frotasPro.api.modules.logistica.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.frotasPro.api.shared.enums.TipoParada;

@Getter
@Setter
public class ParadaResumoResponse {

    private UUID id;
    private String numeroCarga;
    private TipoParada tipoParada;
    private LocalDateTime dtInicio;
    private LocalDateTime dtFim;
    private String cidade;
    private String local;
    private Integer kmOdometro;
}
