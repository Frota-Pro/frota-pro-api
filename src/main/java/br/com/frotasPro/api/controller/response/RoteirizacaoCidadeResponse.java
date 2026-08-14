package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoteirizacaoCidadeResponse {

    private String cidade;

    // Ordem já parametrizada, na sequência em que o motorista deve visitar.
    private List<String> clientesOrdenados;

    // Clientes já vistos em cargas dessa cidade mas que ainda não entraram
    // na ordem — o que falta parametrizar.
    private List<String> clientesSemPosicao;

    /** Null = sem override — usa o padrão global de Parâmetros do Sistema. */
    private Integer tempoMinimoEntregaMinutos;
}
