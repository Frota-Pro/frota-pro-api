package br.com.frotasPro.api.domain.enums;

/**
 * Classifica cada linha do relatório mensal do motorista conforme a relação
 * entre quem dirigiu a carga e o titular do caminhão usado — ver
 * RelatorioMetaMensalMotoristaService. Km rodado/km-por-litro são
 * responsabilidade de quem é titular do caminhão (cuida da economia dele,
 * empreste ou não); tonelada é sempre de quem efetivamente dirigiu e
 * entregou a carga.
 */
public enum TipoLinhaRelatorioMotorista {

    /** Motorista é titular do caminhão e foi ele quem dirigiu — conta tudo, como sempre. */
    PROPRIA,

    /**
     * O motorista dirigiu, mas o caminhão tem outro titular — conta tonelada
     * (ele entregou a carga), mas km rodado/km-por-litro não contam pra ele:
     * vão pro relatório do titular do caminhão.
     */
    CAMINHAO_DE_OUTRO_TITULAR,

    /**
     * O motorista dirigiu um caminhão sem titular cadastrado — conta
     * tonelada, mas km rodado/km-por-litro não contam pra ninguém (sem
     * titular, não tem pra quem atribuir a economia do caminhão).
     */
    CAMINHAO_SEM_TITULAR,

    /**
     * Outro motorista dirigiu o caminhão deste motorista (que é o titular) —
     * conta km rodado/km-por-litro pra ele (é o caminhão dele), mas não
     * conta tonelada (quem entregou foi o outro).
     */
    MOTORISTA_TERCEIRO_NO_MEU_CAMINHAO
}
