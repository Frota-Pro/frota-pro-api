package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Resumo de quanto peso/valor uma carga perdeu (devolução + transferência
 * "perdida") e recebeu (transferência "recebida"), comparado com o que está
 * gravado nela hoje — pra deixar claro se o desconto foi aplicado ou
 * bloqueado pelo parâmetro de proteção. Montado ao vivo a partir do WinThor,
 * nada fica guardado aqui.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumoDescontoCargaResponse {

    /** O que está gravado hoje na carga (Carga.pesoCarga convertido pra kg / Carga.valorTotal). */
    private BigDecimal pesoAtualKg;
    private BigDecimal valorAtual;

    /** Soma de devoluções + notas transferidas como "perdida" encontradas no WinThor agora. */
    private BigDecimal pesoPerdidoKg;
    private BigDecimal valorPerdido;

    /** Soma de notas transferidas como "recebida" encontradas no WinThor agora. */
    private BigDecimal pesoRecebidoKg;
    private BigDecimal valorRecebido;

    /**
     * Estimativa de peso/valor "cheio", antes de qualquer devolução/transferência.
     * Se descontoBloqueado, é igual ao atual (nada foi de fato descontado ainda).
     * Senão, é atual + perdido − recebido (reconstrução com base no que está no
     * WinThor agora — pode não bater 100% se algo mudou lá depois do último sync).
     */
    private BigDecimal pesoOriginalKg;
    private BigDecimal valorOriginal;

    /** Espelha Carga.diminuicaoPesoValorBloqueada — true = o último sync bloqueou a diminuição. */
    private boolean descontoBloqueado;

    /** true se há alguma devolução/transferência (perdida ou recebida) encontrada no WinThor. */
    private boolean houveMovimentacao;

    /** Frase pronta pra exibir na tela explicando a situação. */
    private String mensagem;
}
