package br.com.frotasPro.api.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CaminhaoRequest {

    @Size(max = 50, message = "Código externo deve ter no máximo 50 caracteres")
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
    @Pattern(
            regexp = "^([A-Za-z]{3}-?\\d{4}|[A-Za-z]{3}\\d[A-Za-z0-9]\\d{2})$",
            message = "Placa inválida (padrão antigo ou Mercosul)"
    )
    private String placa;

    @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
    private String cor;

    @Size(max = 30, message = "ANTT deve ter no máximo 30 caracteres")
    private String antt;

    @Size(max = 30, message = "RENAVAN deve ter no máximo 30 caracteres")
    private String renavan;

    @Size(max = 30, message = "Chassi deve ter no máximo 30 caracteres")
    private String chassi;

    @DecimalMin(value = "0.00", message = "Tara deve ser >= 0")
    private BigDecimal tara;

    @DecimalMin(value = "0.00", message = "Peso máximo deve ser >= 0")
    private BigDecimal maxPeso;

    @Size(max = 20, message = "Categoria inválida")
    private String categoria;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dtLicenciamento;
}
