package br.com.frotasPro.api.controller.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Conferência mensal do valor TRANSPORTADO: o que o WinThor diz que foi transportado no mês, o que o
 * FrotaPRO tem, e cada centavo da diferença explicado por nota ou por carga. Os valores das pontes
 * vêm com sinal: somados ao ponto de partida chegam exatamente ao ponto final.
 */
public record ConferenciaTransportadoResponse(
        String mes,
        OffsetDateTime geradoEm,
        Totais totais,
        List<LinhaPonte> ponteVendaParaTransportado,
        List<LinhaPonte> ponteWinThorParaFrotaPro,
        List<CargaDiferenca> cargasComDiferenca,
        List<NotaFora> notasForaDoTransporte,
        int notasForaTotal,
        boolean fechou,
        ResumoMovimentos resumoMovimentos,
        List<Movimento> movimentosEntreCargas,
        int limitePequenaKg,
        List<CargaPequena> cargasPequenas,
        List<LancamentoManual> lancamentoManual,
        List<ManifestoSemNota> manifestosSemNota,
        ResumoDevolucoes resumoDevolucoes,
        List<DevolucaoNota> devolucoes
) {

    /** {@code vendaFaturada} é só referência (regra da rotina 111); o que vale é o transportado. */
    public record Totais(
            BigDecimal vendaFaturada,
            long notasVendaFaturada,
            BigDecimal transportadoWinThor,
            long cargasWinThor,
            BigDecimal transportadoFrotaPro,
            long cargasFrotaPro,
            BigDecimal diferenca
    ) {
    }

    public record LinhaPonte(String codigo, String titulo, String explicacao, long quantidade, BigDecimal valor) {
    }

    public record CargaDiferenca(
            Integer numCar,
            String categoria,
            String explicacao,
            BigDecimal valorWinThor,
            BigDecimal valorFrotaPro,
            BigDecimal diferenca,
            LocalDate dataWinThor,
            LocalDate dataFrotaPro,
            String statusFrotaPro,
            List<NotaDetalhe> notas,
            boolean podeSerZerada
    ) {

        public CargaDiferenca(Integer numCar, String categoria, String explicacao, BigDecimal valorWinThor,
                              BigDecimal valorFrotaPro, BigDecimal diferenca, LocalDate dataWinThor, LocalDate dataFrotaPro,
                              String statusFrotaPro, List<NotaDetalhe> notas) {
            this(numCar, categoria, explicacao, valorWinThor, valorFrotaPro, diferenca, dataWinThor, dataFrotaPro,
                    statusFrotaPro, notas, false);
        }
    }

    /** {@code historico}: transferências da nota entre carregamentos (log do WinThor), da mais antiga para a mais nova. */
    public record NotaDetalhe(long numNota, BigDecimal valor, Integer paraCarga, String motivo, List<TransferenciaInfo> historico) {

        public NotaDetalhe(long numNota, BigDecimal valor, Integer paraCarga, String motivo) {
            this(numNota, valor, paraCarga, motivo, List.of());
        }
    }

    public record TransferenciaInfo(Integer deCarga, Integer paraCarga, LocalDateTime quando, String motivo) {
    }

    /**
     * Notas que saíram de {@code deCarga} (diminui) e entraram em {@code paraCarga} (aumenta): o mesmo valor
     * sai de uma e entra na outra. Agrupado por par de cargas e motivo.
     */
    public record Movimento(Integer deCarga, Integer paraCarga, String motivo, long notas, BigDecimal valor,
                            LocalDateTime primeira, LocalDateTime ultima, List<Long> numerosNotas) {
    }

    public record ResumoMovimentos(long transferencias, long notas, BigDecimal valor) {
    }

    /**
     * Carga de peso baixo para um caminhão: o motorista em geral não se lembra de ter feito. {@code origem} diz
     * como ela nasceu (reentrega, transferência, ficou sem nota ou carga comum) e {@code explicacao} o que fazer.
     */
    public record CargaPequena(Integer numCar, LocalDate dataFaturamento, BigDecimal valor, BigDecimal pesoKg,
                               String status, String motorista, String placa, boolean motoristaExterno,
                               String origem, String origemDe, int notas, String explicacao) {
    }

    /**
     * Carga transportada por motorista que não é da frota (caminhão da frota): km inicial/final e abastecimento
     * precisam ser lançados à mão, porque o motorista não tem acesso ao app.
     */
    public record LancamentoManual(Integer numCar, String codMotorista, BigDecimal valor, int notas,
                                   boolean cargaNoFrotaPro, String explicacao) {
    }

    /** Manifesto sem nota: viagem sem valor, só com o peso do manifesto. Conta como viagem se o caminhão é da frota. */
    public record ManifestoSemNota(Long numMdfe, Integer codMotorista, String codVeiculo, String situacao, LocalDate data,
                                   BigDecimal pesoKg, boolean caminhaoDaFrota, boolean motoristaDaFrota, String explicacao) {
    }

    public record ResumoDevolucoes(long notas, BigDecimal valor, BigDecimal pesoKg) {
    }

    /** Devolução de uma nota do mês: o valor da nota NÃO diminui, a devolução é informada à parte. */
    public record DevolucaoNota(long numNota, Integer numCar, BigDecimal valorNota, BigDecimal valorDevolvido,
                                BigDecimal pesoDevolvidoKg) {
    }

    public record NotaFora(long numNota, LocalDate dtSaida, Integer codCli, Integer condVenda, Integer numCar, BigDecimal valor, String motivo) {
    }
}
