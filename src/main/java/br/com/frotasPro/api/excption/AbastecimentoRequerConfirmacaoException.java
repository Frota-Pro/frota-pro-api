package br.com.frotasPro.api.excption;

import lombok.Getter;

import java.util.List;

/**
 * Lançada quando um abastecimento tem algo fora do padrão (preço/litro ou
 * valor total muito acima da média, odômetro repetido) e o request ainda não
 * veio com confirmação explícita (AbastecimentoRequest.confirmarAvisos). A
 * tela mostra os avisos e, se o usuário confirmar, reenvia o mesmo request
 * com confirmarAvisos=true — aí passa direto.
 */
@Getter
public class AbastecimentoRequerConfirmacaoException extends RuntimeException {

    private final List<String> avisos;

    public AbastecimentoRequerConfirmacaoException(List<String> avisos) {
        super(String.join(" ", avisos));
        this.avisos = avisos;
    }
}
