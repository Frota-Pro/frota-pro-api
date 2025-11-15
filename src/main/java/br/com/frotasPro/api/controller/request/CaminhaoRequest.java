package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CaminhaoRequest {

    private String codigoExterno;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 150, message = "Descrição deve ter entre 3 e 150 caracteres")
    private String descricao;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 2, max = 100, message = "Modelo deve ter entre 2 e 100 caracteres")
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 100, message = "Marca deve ter entre 2 e 100 caracteres")
    private String marca;

    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 8, message = "Placa inválida")
    private String placa;

    private String cor;
    private String antt;
    private String renavan;
    private String chassi;
    private BigDecimal tara;
    private BigDecimal maxPeso;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dtLicenciamento;
}
