package br.com.frotasPro.api.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoteirizacaoCidadeRequest {

    @NotNull(message = "Lista de clientes ordenados é obrigatória")
    private List<
            @NotBlank(message = "Cliente inválido")
                    String
            > clientesOrdenados;

    /** Opcional — null = usa o padrão global definido em Parâmetros do Sistema. */
    @Min(value = 0, message = "Tempo mínimo de entrega deve ser >= 0")
    private Integer tempoMinimoEntregaMinutos;
}
