package br.com.frotasPro.api.modules.integracao.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CargaSyncRequestEvent {

    private UUID jobId;
    private UUID empresaId;

    private LocalDate dataInicial;
    private LocalDate dataFinal;
    private List<Integer> codigosCargas;
    private List<Integer> codigosCaminhoes;
    private List<Integer> codigosMotoristas;

    private String tipoCarga;
    private String origem;
    private String solicitadoPor;

    private OffsetDateTime timestampSolicitacao;
}
