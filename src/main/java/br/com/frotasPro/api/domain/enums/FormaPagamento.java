package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO_DEBITO("Cartão de Débito"),
    CARTAO_CREDITO("Cartão de Crédito"),
    PIX("PIX"),
    TRANSFERENCIA("Transferência Bancária"),
    CHEQUE("Cheque"),
    BOLETO("Boleto Bancário"),
    VALE_COMBUSTIVEL("Vale Combustível"),
    CONVENIO("Convênio"),
    FATURADO("Faturado para Empresa"),
    NOTA_DE_CREDITO("Nota de Crédito"),
    OUTROS("Outros");

    private final String descricao;
}
