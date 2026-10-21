package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Quem já finalizou (e quem não) a meta de um mês: alimenta o gráfico do dashboard e o filtro do relatório. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FechamentoMetaResumoResponse {

    /** Primeiro dia do mês consultado. */
    private LocalDate mesReferencia;
    /** false = parâmetro desligado (dias úteis = 0). */
    private boolean ativo;
    private int diasUteisFechamento;
    /** Último dia em que o mês ainda aceita cargas pra todo mundo. */
    private LocalDate dataLimite;
    /** true quando o mês já terminou. */
    private boolean mesEncerrado;

    private int totalMotoristas;
    /** Motorista (ou gerente) confirmou. */
    private int confirmados;
    /** Ninguém confirmou, mas o prazo geral venceu: o mês fechou sozinho. */
    private int fechadosPeloPrazo;
    /** O mês acabou, ainda dentro do prazo, e o motorista ainda não confirmou. */
    private int emAberto;
    /** O mês ainda não terminou. */
    private int emAndamento;
    /** Confirmados + fechados pelo prazo: quem já pode ter o relatório do mês impresso. */
    private int finalizados;

    private List<ItemMotorista> motoristas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemMotorista {
        private String codigo;
        private String nome;
        /** DESATIVADO | EM_ANDAMENTO | ABERTO | CONFIRMADO | FECHADO. */
        private String situacao;
        /** O mês dele já está fechado (confirmado ou prazo vencido). */
        private boolean finalizado;
        private LocalDateTime confirmadoEm;
        /** APP ou ADMIN. */
        private String origem;
    }
}
