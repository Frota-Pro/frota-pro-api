package br.com.frotasPro.api.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TipoDespesa {
    // --- Despesas de viagem / motorista ---
    ALIMENTACAO("Alimentação"),
    PERNOITE("Pernoite"),
    HOSPEDAGEM("Hospedagem"),
    PEDAGIO("Pedágio"),
    ESTACIONAMENTO("Estacionamento"),
    LAVAGEM("Lavagem de Caminhão"),
    OUTROS_VIAGEM("Outros (Viagem)"),

    // --- Abastecimento ---
    COMBUSTIVEL("Combustível"),
    ARLA32("ARLA 32"),

    // --- Manutenção ---
    MANUTENCAO_CORRETIVA("Manutenção Corretiva"),
    MANUTENCAO_PREVENTIVA("Manutenção Preventiva"),
    PNEU("Compra / Troca de Pneus"),
    OLEO("Troca de Óleo / Lubrificação"),
    PECAS("Peças Diversas"),
    SERVICOS("Serviços Gerais de Oficina"),

    // --- Administrativo / Operacional ---
    DOCUMENTACAO("Documentação / Licenciamento"),
    MULTA("Multas"),
    SEGURO("Seguro"),
    DESPESA_INTERNA("Despesa Interna"),
    OUTROS("Outros");

    private final String descricao;
}
