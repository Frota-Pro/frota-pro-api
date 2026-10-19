package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Situação do fechamento da meta de um motorista num mês (usado pelo app e pelo painel). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FechamentoMetaResponse {

    /** Primeiro dia do mês consultado. */
    private LocalDate mesReferencia;

    /** false = parâmetro desligado (dias úteis = 0): o fechamento por confirmação não existe. */
    private boolean ativo;

    private int diasUteisFechamento;

    /** Último dia em que o mês ainda aceita cargas pra todo mundo. */
    private LocalDate dataLimite;

    /**
     * DESATIVADO | EM_ANDAMENTO (mês ainda não acabou) | ABERTO (acabou, dentro do prazo —
     * dá pra confirmar) | CONFIRMADO (motorista fechou) | FECHADO (prazo geral venceu).
     */
    private String situacao;

    private boolean podeConfirmar;
    private boolean confirmadoPeloMotorista;
    private LocalDateTime confirmadoEm;
    private String origem;
}
