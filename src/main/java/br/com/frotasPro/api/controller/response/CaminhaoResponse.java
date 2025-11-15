package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaminhaoResponse {

    private String codigo;
    private String codigoExterno;
    private String descricao;
    private String modelo;
    private String marca;
    private String placa;
    private String cor;
    private String antt;
    private String renavan;
    private String chassi;
    private BigDecimal tara;
    private BigDecimal maxPeso;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dtLicenciamento;

    private Status status;
    private boolean ativo;
}
