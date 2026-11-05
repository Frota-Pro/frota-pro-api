package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.CargaDiferenca;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.CargaPequena;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.DevolucaoNota;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.LancamentoManual;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.ManifestoSemNota;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.ResumoDevolucoes;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.LinhaPonte;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.NotaDetalhe;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.Movimento;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.NotaFora;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.ResumoMovimentos;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.Totais;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.TransferenciaInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Cálculo da conferência do valor TRANSPORTADO de um mês. Função pura: recebe as notas do WinThor e as
 * cargas do FrotaPRO e devolve as duas pontes com sinal, de modo que
 *
 * <pre>
 *   venda faturada (referência da 111) + ponte 1 = transportado no WinThor
 *   transportado no WinThor           + ponte 2 = transportado no FrotaPRO
 * </pre>
 *
 * fecham ao centavo. Nenhuma diferença vira "ajuste": o que não se consegue ligar a uma nota fica numa
 * linha própria e nomeada ({@link #VALOR_DESATUALIZADO}).
 *
 * <p>Uma nota é TRANSPORTADA quando não está cancelada, pertence a um carregamento que existe e tem pedido,
 * consta num MDF-e e tem motorista e veículo: as mesmas exigências com que a integradora cria a carga.
 * Nota sem carregamento ou sem manifesto não entra. O valor transportado de um carregamento é a soma
 * das notas transportadas dele (em qualquer mês) e a data dele é a da última dessas notas.
 */
public final class ConferenciaTransportadoCalculo {

    public static final String VALOR_DESATUALIZADO = "VALOR_DESATUALIZADO";

    private static final int LIMITE_NOTAS_FORA = 500;

    private ConferenciaTransportadoCalculo() {
    }

    public record Nota(long numNota, String especie, LocalDate dtSaida, Integer codCli, Integer condVenda, Integer numCar,
                       BigDecimal vlTotal, boolean cancelada, boolean temMotoristaVeiculo, boolean temMdfe,
                       boolean temPedido, boolean temCarregamento,
                       Integer codMotoristaMdfe, String codVeiculoMdfe,
                       boolean caminhaoForaDaFrota, boolean motoristaForaDaFrota) {

        public Nota(long numNota, String especie, LocalDate dtSaida, Integer codCli, Integer condVenda, Integer numCar,
                    BigDecimal vlTotal, boolean cancelada, boolean temMotoristaVeiculo, boolean temMdfe,
                    boolean temPedido, boolean temCarregamento) {
            this(numNota, especie, dtSaida, codCli, condVenda, numCar, vlTotal, cancelada, temMotoristaVeiculo, temMdfe,
                    temPedido, temCarregamento, null, null, false, false);
        }

        BigDecimal valor() {
            return vlTotal == null ? BigDecimal.ZERO : vlTotal;
        }

        boolean comCarregamento() {
            return numCar != null && numCar > 1;
        }

        /**
         * Cruza o motorista e o caminhão do manifesto com o cadastro da frota. Caminhão que não é da frota: a
         * nota é de terceiro e não entra. Motorista que não é da frota, com caminhão da frota: entra, mas km e
         * abastecimento são lançados à mão.
         */
        Nota comFrota(Frota frota) {
            if (frota == null || !temMdfe) return this;
            boolean caminhaoFora = codVeiculoMdfe != null && !frota.temCaminhao(codVeiculoMdfe);
            boolean motoristaFora = codMotoristaMdfe != null && !frota.temMotorista(codMotoristaMdfe);
            return new Nota(numNota, especie, dtSaida, codCli, condVenda, numCar, vlTotal, cancelada, temMotoristaVeiculo,
                    temMdfe, temPedido, temCarregamento, codMotoristaMdfe, codVeiculoMdfe, caminhaoFora, motoristaFora);
        }

        /** Código do motivo de a nota NÃO ser transportada; nulo quando ela é transportada. */
        String motivo() {
            if (cancelada) return "CANCELADA";
            if (!comCarregamento()) return "SEM_CARREGAMENTO";
            if (!temCarregamento) return "CARREGAMENTO_INEXISTENTE";
            if (!temPedido) return "SEM_PEDIDO";
            if (!temMdfe) return "SEM_MDFE";
            if (!temMotoristaVeiculo) return "SEM_MOTORISTA_VEICULO";
            if (caminhaoForaDaFrota) return "CAMINHAO_FORA_DA_FROTA";
            return null;
        }

        boolean transportada() {
            return motivo() == null;
        }
    }

    public record CargaFp(Integer numCar, LocalDate dataFaturamento, BigDecimal valorTotal, String status,
                          boolean naoEncontrada, boolean diminuicaoBloqueada, Set<Long> notas,
                          BigDecimal pesoKg, String motorista, String placa, boolean motoristaExterno,
                          String origemCarga, String origemNumero, BigDecimal valorWinThor, boolean transferida) {

        public CargaFp(Integer numCar, LocalDate dataFaturamento, BigDecimal valorTotal, String status,
                       boolean naoEncontrada, boolean diminuicaoBloqueada, Set<Long> notas) {
            this(numCar, dataFaturamento, valorTotal, status, naoEncontrada, diminuicaoBloqueada, notas,
                    null, null, null, false, null, null, null, false);
        }

        BigDecimal valor() {
            return valorTotal == null ? BigDecimal.ZERO : valorTotal;
        }
    }

    /**
     * Regras da venda faturada de referência (rotina 111): configuráveis porque são da empresa, não do sistema.
     * O TV10 não é excluído do transportado: entra quando o manifesto é da frota.
     */
    public record Regras(Set<Integer> tvExcluidas, Set<Integer> tvZeradas, Set<Integer> clientesIgnorados) {
    }

    /** Códigos externos (do WinThor) dos caminhões e motoristas da FROTA cadastrados no FrotaPRO (motorista externo não conta). */
    public record Frota(Set<String> caminhoes, Set<String> motoristas) {

        boolean temCaminhao(String codigo) {
            return codigo != null && caminhoes.contains(codigo.trim());
        }

        boolean temMotorista(Integer codigo) {
            return codigo != null && motoristas.contains(String.valueOf(codigo));
        }
    }

    /** Uma linha do log de transferência da nota entre carregamentos (PCLOGTRANSFNFCARREG). */
    public record Transferencia(long numNota, Integer deCarga, Integer paraCarga, LocalDateTime quando, String motivo) {
    }

    public record ManifestoIn(Long numMdfe, Integer codMotorista, String codVeiculo, String situacao, LocalDate data, BigDecimal pesoKg) {
    }

    public record DevolucaoIn(long numNota, BigDecimal valor, BigDecimal pesoKg) {
    }

    /** O que vem além das notas e das cargas: log de transferência, cadastro da frota, descartadas, manifestos, devoluções. */
    public record Extras(List<Transferencia> transferencias, Frota frota, Map<Integer, String> descartadas,
                         List<ManifestoIn> manifestos, List<DevolucaoIn> devolucoes, BigDecimal limitePequenaKg) {

        public static Extras vazio() {
            return new Extras(List.of(), null, Map.of(), List.of(), List.of(), BigDecimal.valueOf(500));
        }

        public Extras comTransferencias(List<Transferencia> t) {
            return new Extras(t, frota, descartadas, manifestos, devolucoes, limitePequenaKg);
        }
    }

    public record Resultado(Totais totais,
                            List<LinhaPonte> ponteVendaParaTransportado,
                            List<LinhaPonte> ponteWinThorParaFrotaPro,
                            List<CargaDiferenca> cargasComDiferenca,
                            List<NotaFora> notasFora,
                            int notasForaTotal,
                            boolean fechou,
                            ResumoMovimentos resumoMovimentos,
                            List<Movimento> movimentos,
                            List<CargaPequena> cargasPequenas,
                            List<LancamentoManual> lancamentoManual,
                            List<ManifestoSemNota> manifestosSemNota,
                            ResumoDevolucoes resumoDevolucoes,
                            List<DevolucaoNota> devolucoes) {
    }

    private record CargaWt(Integer numCar, BigDecimal valor, LocalDate data, List<Nota> todas, List<Nota> transportadas) {
    }

    /** Acumula uma linha de ponte (quantidade e valor com sinal). */
    private static final class Acum {
        long quantidade;
        BigDecimal valor = BigDecimal.ZERO;

        void soma(BigDecimal v, long q) {
            valor = valor.add(v);
            quantidade += q;
        }
    }

    public static Resultado calcular(YearMonth mes, List<Nota> notas, List<CargaFp> cargasFp, Regras regras) {
        return calcular(mes, notas, cargasFp, regras, Extras.vazio());
    }

    public static Resultado calcular(YearMonth mes, List<Nota> notas, List<Transferencia> transferencias,
                                     List<CargaFp> cargasFp, Regras regras) {
        return calcular(mes, notas, cargasFp, regras, Extras.vazio().comTransferencias(transferencias));
    }

    public static Resultado calcular(YearMonth mes, List<Nota> notasBrutas, List<CargaFp> cargasFp, Regras regras,
                                     Extras extras) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        List<Transferencia> transferencias = extras.transferencias();
        Map<Long, List<TransferenciaInfo>> historico = historicoPorNota(transferencias);
        List<Nota> notas = notasBrutas.stream().map(n -> n.comFrota(extras.frota())).toList();

        Map<Long, Nota> notaPorNumero = new HashMap<>();
        Map<Integer, List<Nota>> notasPorCarga = new LinkedHashMap<>();
        for (Nota n : notas) {
            notaPorNumero.putIfAbsent(n.numNota(), n);
            if (n.comCarregamento()) {
                notasPorCarga.computeIfAbsent(n.numCar(), k -> new ArrayList<>()).add(n);
            }
        }

        // Cargas do WinThor: carregamentos com ao menos uma nota transportada.
        Map<Integer, CargaWt> cargasWt = new LinkedHashMap<>();
        for (var e : notasPorCarga.entrySet()) {
            List<Nota> transp = e.getValue().stream().filter(Nota::transportada).toList();
            if (transp.isEmpty()) continue;
            BigDecimal valor = transp.stream().map(Nota::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDate data = transp.stream().map(Nota::dtSaida).filter(d -> d != null).max(Comparator.naturalOrder()).orElse(null);
            cargasWt.put(e.getKey(), new CargaWt(e.getKey(), valor, data, e.getValue(), transp));
        }
        Map<Integer, CargaWt> cargasWtDoMes = new LinkedHashMap<>();
        cargasWt.forEach((k, c) -> {
            if (dentro(c.data(), inicio, fim)) cargasWtDoMes.put(k, c);
        });

        Map<Integer, CargaFp> fpPorCarga = new HashMap<>();
        for (CargaFp c : cargasFp) {
            if (c.numCar() != null) fpPorCarga.putIfAbsent(c.numCar(), c);
        }
        Map<Integer, CargaFp> fpDoMes = new LinkedHashMap<>();
        fpPorCarga.forEach((k, c) -> {
            if (dentro(c.dataFaturamento(), inicio, fim)) fpDoMes.put(k, c);
        });

        // ---------- venda faturada de referência e ponte 1 (venda -> transportado) ----------
        BigDecimal vendaFaturada = BigDecimal.ZERO;
        long notasVenda = 0;
        Map<String, Acum> ponte1 = new LinkedHashMap<>();
        List<NotaFora> fora = new ArrayList<>();
        int foraTotal = 0;

        for (Nota n : notas) {
            boolean doMes = dentro(n.dtSaida(), inicio, fim);
            CargaWt carga = n.comCarregamento() ? cargasWt.get(n.numCar()) : null;
            boolean cargaDoMes = carga != null && cargasWtDoMes.containsKey(n.numCar());
            boolean transportada = n.transportada();

            if (doMes) {
                boolean naVenda = naVendaFaturada(n, regras);
                BigDecimal vf = naVenda ? valorNaVenda(n, regras) : BigDecimal.ZERO;
                if (naVenda) {
                    vendaFaturada = vendaFaturada.add(vf);
                    notasVenda++;
                    if (!transportada) {
                        acum(ponte1, "NAO_TRANSPORTADA_" + n.motivo()).soma(vf.negate(), 1);
                    } else if (!cargaDoMes) {
                        acum(ponte1, "EM_CARGA_DE_OUTRO_MES").soma(vf.negate(), 1);
                    }
                }
                if (!transportada) {
                    foraTotal++;
                    if (fora.size() < LIMITE_NOTAS_FORA || n.valor().compareTo(BigDecimal.ZERO) > 0) {
                        fora.add(new NotaFora(n.numNota(), n.dtSaida(), n.codCli(), n.condVenda(), n.numCar(), n.valor(), n.motivo()));
                    }
                }
            }

            if (transportada && cargaDoMes) {
                boolean noMes = doMes;
                boolean naVenda = noMes && naVendaFaturada(n, regras);
                BigDecimal vf = naVenda ? valorNaVenda(n, regras) : BigDecimal.ZERO;
                // o que a venda de referência não trouxe desta nota e o transporte traz
                BigDecimal extra = n.valor().subtract(vf);
                if (!noMes) {
                    acum(ponte1, "NOTA_DE_OUTRO_MES_NA_CARGA").soma(n.valor(), 1);
                } else if (naVenda && contem(regras.tvZeradas(), n.condVenda())) {
                    acum(ponte1, "BONIFICACAO_TRANSPORTADA").soma(extra, 1);
                } else if (!naVenda) {
                    acum(ponte1, "TRANSPORTADA_FORA_DA_VENDA").soma(extra, 1);
                }
            }
        }

        fora.sort(Comparator.comparing(NotaFora::valor).reversed());
        List<NotaFora> foraLimitado = fora.size() > LIMITE_NOTAS_FORA ? new ArrayList<>(fora.subList(0, LIMITE_NOTAS_FORA)) : fora;

        BigDecimal transportadoWt = cargasWtDoMes.values().stream().map(CargaWt::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transportadoFp = fpDoMes.values().stream().map(CargaFp::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ---------- ponte 2 (WinThor -> FrotaPRO), carga a carga ----------
        Map<String, Acum> ponte2 = new LinkedHashMap<>();
        List<CargaDiferenca> diferencas = new ArrayList<>();

        Set<Integer> todas = new java.util.LinkedHashSet<>();
        todas.addAll(cargasWtDoMes.keySet());
        todas.addAll(fpDoMes.keySet());

        for (Integer numCar : todas) {
            CargaWt wt = cargasWtDoMes.get(numCar);
            CargaFp fp = fpPorCarga.get(numCar);
            boolean fpNoMes = fpDoMes.containsKey(numCar);

            if (wt != null && fp == null) {
                String descartada = extras.descartadas().get(numCar);
                String codigoNs = descartada != null ? "CARGA_DESCARTADA_NO_SYNC" : "CARGA_NAO_SINCRONIZADA";
                acum(ponte2, codigoNs).soma(wt.valor().negate(), 1);
                diferencas.add(new CargaDiferenca(numCar, codigoNs,
                        descartada != null
                                ? "O sync descartou esta carga: " + descartada + ". Cadastre o que falta para ela entrar."
                                : "Existe no WinThor (MDF-e emitido) e ainda não chegou ao FrotaPRO.",
                        wt.valor(), BigDecimal.ZERO, wt.valor().negate(), wt.data(), null, null,
                        detalheNotas(wt.transportadas(), null)));
            } else if (wt != null && !fpNoMes) {
                acum(ponte2, "DATA_DIFERENTE_NO_FROTAPRO").soma(wt.valor().negate(), 1);
                diferencas.add(new CargaDiferenca(numCar, "DATA_DIFERENTE_NO_FROTAPRO",
                        "No WinThor a última nota é de " + wt.data() + "; no FrotaPRO a carga está datada em " + fp.dataFaturamento() + ".",
                        wt.valor(), BigDecimal.ZERO, wt.valor().negate(), wt.data(), fp.dataFaturamento(), fp.status(), List.of()));
            } else if (wt != null) {
                comparar(numCar, wt, fp, notaPorNumero, historico, ponte2, diferencas);
            } else {
                // só no FrotaPRO neste mês
                CargaWt outro = cargasWt.get(numCar);
                if (outro != null) {
                    acum(ponte2, "DATA_DIFERENTE_NO_WINTHOR").soma(fp.valor(), 1);
                    diferencas.add(new CargaDiferenca(numCar, "DATA_DIFERENTE_NO_WINTHOR",
                            "No FrotaPRO está em " + fp.dataFaturamento() + ", mas no WinThor a última nota transportada é de " + outro.data() + " (outro mês).",
                            outro.valor(), fp.valor(), fp.valor(), outro.data(), fp.dataFaturamento(), fp.status(), List.of()));
                } else if (fp.transferida() && fp.valor().signum() == 0) {
                    // já foi zerada dos totais porque as notas foram para outras cargas: nada a explicar
                    continue;
                } else {
                    List<NotaDetalhe> destinos = destinosDasNotas(fp, notaPorNumero, historico);
                    boolean soTerceiro = !destinos.isEmpty()
                            && destinos.stream().allMatch(d -> "CAMINHAO_FORA_DA_FROTA".equals(d.motivo()));
                    String codigo = soTerceiro ? "CARGA_DE_TERCEIRO"
                            : fp.naoEncontrada() ? "CARGA_SUMIDA_DO_WINTHOR" : "CARGA_SEM_TRANSPORTE_HOJE";
                    String texto = soTerceiro
                            ? "O caminhão do manifesto não é da frota (transporte de terceiro): a carga não entra no transportado"
                            : fp.naoEncontrada()
                            ? "O carregamento ficou sem nenhuma nota no WinThor"
                            : "Nenhuma nota desta carga é transportada hoje";
                    long movidas = destinos.stream().filter(d -> d.paraCarga() != null).count();
                    long comLog = destinos.stream()
                            .filter(d -> d.paraCarga() != null && saiuDaCarga(d.historico(), numCar)).count();
                    if (movidas > 0) {
                        texto += "; " + movidas + " de " + destinos.size() + " nota(s) estão hoje em outro carregamento ("
                                + comLog + " com transferência registrada no log do WinThor"
                                + (comLog < movidas ? ", " + (movidas - comLog) + " sem registro" : "")
                                + "), então o valor está contado duas vezes.";
                    } else {
                        texto += ".";
                    }
                    // só dá para zerar sozinha a carga que ninguém iniciou, ficou sem nota e cujas notas
                    // foram TODAS para outras cargas, com a saída registrada no log do WinThor
                    boolean podeZerar = fp.naoEncontrada() && "SINCRONIZADA".equals(fp.status()) && fp.valor().signum() > 0
                            && movidas > 0 && movidas == destinos.size() && comLog == movidas;
                    acum(ponte2, codigo).soma(fp.valor(), 1);
                    diferencas.add(new CargaDiferenca(numCar, codigo, texto, BigDecimal.ZERO, fp.valor(), fp.valor(),
                            null, fp.dataFaturamento(), fp.status(), destinos, podeZerar));
                }
            }
        }

        diferencas.sort(Comparator.comparing((CargaDiferenca d) -> d.diferenca().abs()).reversed());

        List<LinhaPonte> linhas1 = montarLinhas(ponte1, ORDEM_PONTE1);
        List<LinhaPonte> linhas2 = montarLinhas(ponte2, ORDEM_PONTE2);

        boolean fecha1 = vendaFaturada.add(soma(linhas1)).compareTo(transportadoWt) == 0;
        boolean fecha2 = transportadoWt.add(soma(linhas2)).compareTo(transportadoFp) == 0;

        Totais totais = new Totais(vendaFaturada, notasVenda, transportadoWt, cargasWtDoMes.size(),
                transportadoFp, fpDoMes.size(), transportadoFp.subtract(transportadoWt));

        List<Movimento> movimentos = movimentos(transferencias, notaPorNumero, inicio);
        long notasMovidas = movimentos.stream().mapToLong(Movimento::notas).sum();
        BigDecimal valorMovido = movimentos.stream().map(Movimento::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        long qtdTransf = transferencias.stream().filter(t -> !t.quando().toLocalDate().isBefore(inicio)).count();

        Set<Integer> zeraveis = new java.util.HashSet<>();
        diferencas.stream().filter(CargaDiferenca::podeSerZerada).forEach(d -> zeraveis.add(d.numCar()));

        DevolucoesCalculadas devol = devolucoes(extras.devolucoes(), notaPorNumero);

        return new Resultado(totais, linhas1, linhas2, diferencas, foraLimitado, foraTotal, fecha1 && fecha2,
                new ResumoMovimentos(qtdTransf, notasMovidas, valorMovido), movimentos,
                cargasPequenas(fpDoMes.values(), extras.limitePequenaKg(), zeraveis),
                lancamentoManual(cargasWtDoMes, fpPorCarga),
                manifestosSemNota(extras.manifestos(), extras.frota()),
                devol.resumo(), devol.lista());
    }

    // ------------------------------------------------------------------ cargas pequenas, lançamento manual, manifestos, devoluções

    /**
     * Carga de peso baixo: um caminhão de dez toneladas não faz viagem para 100 kg. Quase sempre é carga de
     * reentrega (um pedido que voltou e ganhou carga própria) ou sobra de transferência; o motorista não lembra
     * porque a entrega foi feita em outra viagem. A explicação diz de onde ela veio e o que conferir.
     */
    private static List<CargaPequena> cargasPequenas(java.util.Collection<CargaFp> cargas, BigDecimal limiteKg, Set<Integer> zeraveis) {
        List<CargaPequena> r = new ArrayList<>();
        for (CargaFp c : cargas) {
            if (c.transferida() || c.pesoKg() == null || c.pesoKg().signum() <= 0 || c.pesoKg().compareTo(limiteKg) >= 0) continue;

            String origem;
            String texto;
            if (c.naoEncontrada()) {
                origem = "SEM_NOTA";
                texto = "O carregamento ficou sem nenhuma nota no WinThor: as notas foram para outras cargas e esta é só o resto."
                        + (zeraveis.contains(c.numCar()) ? " Pode ser zerada dos totais (botão da conferência)." : " Confira para onde as notas foram.");
            } else if ("REENTREGA".equals(c.origemCarga())) {
                origem = "REENTREGA";
                texto = "Carga de reentrega: o pedido voltou" + (c.origemNumero() != null ? " da carga " + c.origemNumero() : "")
                        + " e ganhou carga própria, por isso o peso é baixo. Confirme com o motorista se essa entrega foi feita"
                        + " por ele ou junto de outra viagem; se não houve viagem, o manifesto deveria ser cancelado no WinThor.";
            } else if ("TRANSFERENCIA".equals(c.origemCarga())) {
                origem = "TRANSFERENCIA";
                texto = "Sobra de transferência de pedido" + (c.origemNumero() != null ? " da carga " + c.origemNumero() : "")
                        + ": restou pouco peso nesta carga. Confirme se ainda houve viagem.";
            } else {
                origem = "COMUM";
                texto = "Carga pequena sem origem de reentrega ou transferência no log do WinThor: confirme com o motorista se a viagem existiu.";
            }
            r.add(new CargaPequena(c.numCar(), c.dataFaturamento(), c.valor(), c.pesoKg(), c.status(), c.motorista(),
                    c.placa(), c.motoristaExterno(), origem, c.origemNumero(), c.notas().size(), texto));
        }
        r.sort(Comparator.comparing(CargaPequena::pesoKg));
        return r;
    }

    /** Cargas transportadas por motorista que não é da frota (caminhão da frota): km e abastecimento só à mão. */
    private static List<LancamentoManual> lancamentoManual(Map<Integer, CargaWt> cargasWtDoMes, Map<Integer, CargaFp> fpPorCarga) {
        List<LancamentoManual> r = new ArrayList<>();
        cargasWtDoMes.forEach((numCar, cw) -> {
            Nota ref = cw.transportadas().stream().filter(Nota::motoristaForaDaFrota).findFirst().orElse(null);
            if (ref == null) return;
            boolean noFrotaPro = fpPorCarga.containsKey(numCar);
            String cod = String.valueOf(ref.codMotoristaMdfe());
            r.add(new LancamentoManual(numCar, cod, cw.valor(), cw.transportadas().size(), noFrotaPro,
                    noFrotaPro
                            ? "Motorista " + cod + " não é da frota e não usa o app: lançar km inicial/final e abastecimento à mão."
                            : "Motorista " + cod + " não cadastrado e a carga NÃO está no FrotaPRO (o sync a descartou). "
                            + "Cadastre o motorista para ela entrar; km e abastecimento serão lançados à mão."));
        });
        r.sort(Comparator.comparing(LancamentoManual::valor).reversed());
        return r;
    }

    private static List<ManifestoSemNota> manifestosSemNota(List<ManifestoIn> manifestos, Frota frota) {
        List<ManifestoSemNota> r = new ArrayList<>();
        for (ManifestoIn m : manifestos) {
            boolean caminhaoOk = frota != null && frota.temCaminhao(m.codVeiculo());
            boolean motoristaOk = frota != null && frota.temMotorista(m.codMotorista());
            String texto = caminhaoOk
                    ? "Manifesto sem nota, caminhão da frota: viagem sem valor" + (m.pesoKg() != null ? ", com o peso do manifesto" : "")
                    + (motoristaOk ? "." : ". Motorista fora da frota: lançar km e abastecimento à mão.")
                    : "Manifesto sem nota com caminhão que não é da frota: não é viagem da frota.";
            r.add(new ManifestoSemNota(m.numMdfe(), m.codMotorista(), m.codVeiculo(), m.situacao(), m.data(), m.pesoKg(),
                    caminhaoOk, motoristaOk, texto));
        }
        r.sort(Comparator.comparing((ManifestoSemNota m) -> !m.caminhaoDaFrota()).thenComparing(ManifestoSemNota::numMdfe));
        return r.size() > 300 ? new ArrayList<>(r.subList(0, 300)) : r;
    }

    private record DevolucoesCalculadas(ResumoDevolucoes resumo, List<DevolucaoNota> lista) {
    }

    /** A devolução NÃO diminui o valor da nota (o WinThor mantém o vltotal): entra como informação, com valor e peso. */
    private static DevolucoesCalculadas devolucoes(List<DevolucaoIn> devolucoes, Map<Long, Nota> notaPorNumero) {
        BigDecimal valor = BigDecimal.ZERO;
        BigDecimal peso = BigDecimal.ZERO;
        List<DevolucaoNota> lista = new ArrayList<>();
        for (DevolucaoIn d : devolucoes) {
            BigDecimal v = d.valor() == null ? BigDecimal.ZERO : d.valor();
            BigDecimal pk = d.pesoKg() == null ? BigDecimal.ZERO : d.pesoKg();
            valor = valor.add(v);
            peso = peso.add(pk);
            Nota n = notaPorNumero.get(d.numNota());
            lista.add(new DevolucaoNota(d.numNota(), n == null ? null : n.numCar(), n == null ? null : n.valor(), v, pk));
        }
        lista.sort(Comparator.comparing(DevolucaoNota::valorDevolvido).reversed());
        List<DevolucaoNota> topo = lista.size() > 100 ? new ArrayList<>(lista.subList(0, 100)) : lista;
        return new DevolucoesCalculadas(new ResumoDevolucoes(devolucoes.size(), valor, peso), topo);
    }

    // ------------------------------------------------------------------ transferências entre cargas

    /** Histórico de cada nota, do mais antigo para o mais novo. */
    private static Map<Long, List<TransferenciaInfo>> historicoPorNota(List<Transferencia> transferencias) {
        Map<Long, List<TransferenciaInfo>> r = new HashMap<>();
        transferencias.stream()
                .filter(t -> t.quando() != null)
                .sorted(Comparator.comparing(Transferencia::quando))
                .forEach(t -> r.computeIfAbsent(t.numNota(), k -> new ArrayList<>())
                        .add(new TransferenciaInfo(t.deCarga(), t.paraCarga(), t.quando(), t.motivo())));
        return r;
    }

    /** A nota passou por esta carga e saiu dela (a transferência registrada tem esta carga como origem). */
    private static boolean saiuDaCarga(List<TransferenciaInfo> historico, Integer numCar) {
        return historico != null && historico.stream().anyMatch(h -> java.util.Objects.equals(h.deCarga(), numCar));
    }

    /**
     * Cada transferência tira o valor da nota de uma carga (diminui) e põe em outra (aumenta): o total que sai
     * é, por construção, o total que entra. Agrupado por par de cargas e motivo, só do início do mês em diante.
     */
    private static List<Movimento> movimentos(List<Transferencia> transferencias, Map<Long, Nota> notaPorNumero, LocalDate inicio) {
        record Chave(Integer de, Integer para, String motivo) {
        }
        Map<Chave, List<Transferencia>> grupos = new LinkedHashMap<>();
        for (Transferencia t : transferencias) {
            if (t.quando() == null || t.quando().toLocalDate().isBefore(inicio)) continue;
            grupos.computeIfAbsent(new Chave(t.deCarga(), t.paraCarga(), t.motivo()), k -> new ArrayList<>()).add(t);
        }
        List<Movimento> r = new ArrayList<>();
        grupos.forEach((k, lista) -> {
            Map<Long, Nota> distintas = new LinkedHashMap<>();
            for (Transferencia t : lista) {
                Nota n = notaPorNumero.get(t.numNota());
                if (n != null) distintas.putIfAbsent(t.numNota(), n);
            }
            BigDecimal valor = distintas.values().stream().map(Nota::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDateTime primeira = lista.stream().map(Transferencia::quando).min(Comparator.naturalOrder()).orElse(null);
            LocalDateTime ultima = lista.stream().map(Transferencia::quando).max(Comparator.naturalOrder()).orElse(null);
            r.add(new Movimento(k.de(), k.para(), k.motivo(), lista.stream().map(Transferencia::numNota).distinct().count(),
                    valor, primeira, ultima, lista.stream().map(Transferencia::numNota).distinct().limit(50).toList()));
        });
        r.sort(Comparator.comparing(Movimento::valor).reversed());
        return r;
    }

    // ------------------------------------------------------------------ carga nos dois lados

    private static void comparar(Integer numCar, CargaWt wt, CargaFp fp, Map<Long, Nota> notaPorNumero,
                                 Map<Long, List<TransferenciaInfo>> historico,
                                 Map<String, Acum> ponte2, List<CargaDiferenca> diferencas) {
        BigDecimal delta = fp.valor().subtract(wt.valor());
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 1) notas que o FrotaPRO soma por serem do carregamento, mas que não são transporte hoje
        Map<String, BigDecimal> naoTransp = new TreeMap<>();
        Map<String, Long> naoTranspQtd = new HashMap<>();
        List<NotaDetalhe> detalhes = new ArrayList<>();
        BigDecimal somaNaoTransp = BigDecimal.ZERO;
        for (Nota n : wt.todas()) {
            String m = n.motivo();
            if (m != null) {
                naoTransp.merge("NAO_TRANSPORTADA_SOMADA_" + m, n.valor(), BigDecimal::add);
                naoTranspQtd.merge("NAO_TRANSPORTADA_SOMADA_" + m, 1L, Long::sum);
                somaNaoTransp = somaNaoTransp.add(n.valor());
                detalhes.add(new NotaDetalhe(n.numNota(), n.valor(), null, m));
            }
        }

        // 2) notas que o FrotaPRO ainda tem nesta carga e o WinThor já colocou em outro carregamento
        Set<Long> dasWt = new java.util.HashSet<>();
        wt.todas().forEach(n -> dasWt.add(n.numNota()));
        BigDecimal somaMovidas = BigDecimal.ZERO;
        long qtdMovidas = 0;
        long movidasComLog = 0;
        for (Long numero : fp.notas()) {
            if (dasWt.contains(numero)) continue;
            Nota n = notaPorNumero.get(numero);
            if (n != null) {
                somaMovidas = somaMovidas.add(n.valor());
                qtdMovidas++;
                List<TransferenciaInfo> h = historico.getOrDefault(n.numNota(), List.of());
                if (saiuDaCarga(h, numCar)) movidasComLog++;
                detalhes.add(new NotaDetalhe(n.numNota(), n.valor(), n.numCar(), "MOVIDA_PARA_OUTRA_CARGA", h));
            }
        }

        // 3) notas transportadas que o WinThor tem nesta carga e o FrotaPRO ainda não registrou
        BigDecimal somaNovas = BigDecimal.ZERO;
        long qtdNovas = 0;
        for (Nota n : wt.transportadas()) {
            if (!fp.notas().contains(n.numNota())) {
                somaNovas = somaNovas.add(n.valor());
                qtdNovas++;
                detalhes.add(new NotaDetalhe(n.numNota(), n.valor().negate(), n.numCar(), "NOVA_NO_WINTHOR"));
            }
        }

        naoTransp.forEach((codigo, v) -> acum(ponte2, codigo).soma(v, naoTranspQtd.getOrDefault(codigo, 1L)));
        if (qtdMovidas > 0) acum(ponte2, "NOTAS_MOVIDAS_AINDA_NA_CARGA").soma(somaMovidas, qtdMovidas);
        if (qtdNovas > 0) acum(ponte2, "NOTAS_NOVAS_NAO_REFLETIDAS").soma(somaNovas.negate(), qtdNovas);

        BigDecimal residuo = delta.subtract(somaNaoTransp).subtract(somaMovidas).add(somaNovas);

        // O bloqueio de diminuição segurou o valor maior de propósito: o que o FrotaPRO guardou de WinThor
        // ("valor do WinThor") diz quanto ia diminuir. Fica numa linha própria, não como valor desatualizado.
        BigDecimal travada = BigDecimal.ZERO;
        if (fp.diminuicaoBloqueada() && fp.valorWinThor() != null && residuo.signum() > 0) {
            BigDecimal seguro = fp.valor().subtract(fp.valorWinThor());
            if (seguro.signum() > 0) travada = seguro.min(residuo);
        }
        if (travada.signum() != 0) {
            acum(ponte2, "DIMINUICAO_TRAVADA").soma(travada, 1);
            residuo = residuo.subtract(travada);
        }
        if (residuo.compareTo(BigDecimal.ZERO) != 0) {
            acum(ponte2, VALOR_DESATUALIZADO).soma(residuo, 1);
        }

        List<String> partes = new ArrayList<>();
        if (somaNaoTransp.signum() != 0) partes.add(formatar(somaNaoTransp) + " de notas que não são transporte (cancelada, sem MDF-e...)");
        if (qtdMovidas > 0) {
            partes.add(formatar(somaMovidas) + " de " + qtdMovidas + " nota(s) já movida(s) para outro carregamento ("
                    + movidasComLog + " com transferência registrada no log do WinThor"
                    + (movidasComLog < qtdMovidas ? ", " + (qtdMovidas - movidasComLog) + " sem registro" : "") + ")");
        }
        if (qtdNovas > 0) partes.add("-" + formatar(somaNovas) + " de " + qtdNovas + " nota(s) nova(s) no WinThor");
        if (travada.signum() != 0) partes.add(formatar(travada) + " que o bloqueio de diminuição segurou (o WinThor já diz " + formatar(fp.valorWinThor()) + ")");
        if (residuo.signum() != 0) partes.add(formatar(residuo) + " de valor gravado que não bate com as notas (desatualizado)");
        String explicacao = "FrotaPRO " + (delta.signum() > 0 ? "maior" : "menor") + " que o WinThor em " + formatar(delta.abs())
                + (partes.isEmpty() ? "." : ": " + String.join("; ", partes) + ".");

        diferencas.add(new CargaDiferenca(numCar, "VALOR_DIFERENTE", explicacao, wt.valor(), fp.valor(), delta,
                wt.data(), fp.dataFaturamento(), fp.status(), detalhes));
    }

    private static List<NotaDetalhe> destinosDasNotas(CargaFp fp, Map<Long, Nota> notaPorNumero,
                                                      Map<Long, List<TransferenciaInfo>> historico) {
        List<NotaDetalhe> r = new ArrayList<>();
        for (Long numero : fp.notas()) {
            Nota n = notaPorNumero.get(numero);
            List<TransferenciaInfo> h = historico.getOrDefault(numero, List.of());
            if (n == null) {
                r.add(new NotaDetalhe(numero, BigDecimal.ZERO, null, "FORA_DO_PERIODO_CONSULTADO", h));
            } else if (n.comCarregamento() && !java.util.Objects.equals(n.numCar(), fp.numCar())) {
                r.add(new NotaDetalhe(n.numNota(), n.valor(), n.numCar(), "MOVIDA_PARA_OUTRA_CARGA", h));
            } else {
                String m = n.motivo();
                r.add(new NotaDetalhe(n.numNota(), n.valor(), null, m == null ? "NA_MESMA_CARGA" : m, h));
            }
        }
        return r;
    }


    private static List<NotaDetalhe> detalheNotas(List<Nota> notas, String motivo) {
        return notas.stream()
                .map(n -> new NotaDetalhe(n.numNota(), n.valor(), n.numCar(), motivo == null ? "TRANSPORTADA" : motivo))
                .toList();
    }

    // ------------------------------------------------------------------ regras e utilitários

    private static boolean naVendaFaturada(Nota n, Regras r) {
        return "NF".equalsIgnoreCase(n.especie())
                && !n.cancelada()
                && !contem(r.tvExcluidas(), n.condVenda())
                && !contem(r.clientesIgnorados(), n.codCli());
    }

    private static BigDecimal valorNaVenda(Nota n, Regras r) {
        return contem(r.tvZeradas(), n.condVenda()) ? BigDecimal.ZERO : n.valor();
    }

    /** Set.of(...) lança exceção em contains(null): nota sem TV ou sem cliente simplesmente não está na lista. */
    private static boolean contem(Set<Integer> conjunto, Integer valor) {
        return valor != null && conjunto.contains(valor);
    }

    private static boolean dentro(LocalDate d, LocalDate inicio, LocalDate fim) {
        return d != null && !d.isBefore(inicio) && !d.isAfter(fim);
    }

    private static Acum acum(Map<String, Acum> mapa, String codigo) {
        return mapa.computeIfAbsent(codigo, k -> new Acum());
    }

    private static BigDecimal soma(List<LinhaPonte> linhas) {
        return linhas.stream().map(LinhaPonte::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String formatar(BigDecimal v) {
        return "R$ " + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private static List<LinhaPonte> montarLinhas(Map<String, Acum> mapa, List<String> ordem) {
        List<LinhaPonte> r = new ArrayList<>();
        List<String> codigos = new ArrayList<>(ordem);
        mapa.keySet().stream().filter(c -> !codigos.contains(c)).sorted().forEach(codigos::add);
        for (String codigo : codigos) {
            Acum a = mapa.get(codigo);
            if (a == null || a.valor.signum() == 0) continue;
            String[] t = TEXTOS.getOrDefault(codigo, TEXTOS.get(prefixo(codigo)));
            if (t == null) t = new String[]{codigo, ""};
            r.add(new LinhaPonte(codigo, t[0], t[1], a.quantidade, a.valor));
        }
        return r;
    }

    private static String prefixo(String codigo) {
        for (String p : new String[]{"NAO_TRANSPORTADA_SOMADA_", "NAO_TRANSPORTADA_"}) {
            if (codigo.startsWith(p)) return p;
        }
        return codigo;
    }

    private static final Map<String, String> MOTIVOS = Map.of(
            "CANCELADA", "nota cancelada",
            "SEM_CARREGAMENTO", "sem carregamento (balcão, venda direta, retirada)",
            "CARREGAMENTO_INEXISTENTE", "carregamento inexistente no WinThor",
            "SEM_PEDIDO", "carregamento sem pedido",
            "SEM_MDFE", "sem manifesto (MDF-e)",
            "SEM_MOTORISTA_VEICULO", "sem motorista ou veículo",
            "CAMINHAO_FORA_DA_FROTA", "caminhão do manifesto não é da frota (transporte de terceiro)");

    static String motivoTexto(String codigo) {
        return MOTIVOS.getOrDefault(codigo, codigo);
    }

    private static final List<String> ORDEM_PONTE1 = List.of(
            "NAO_TRANSPORTADA_SEM_CARREGAMENTO", "NAO_TRANSPORTADA_CARREGAMENTO_INEXISTENTE", "NAO_TRANSPORTADA_SEM_PEDIDO",
            "NAO_TRANSPORTADA_SEM_MDFE", "NAO_TRANSPORTADA_CAMINHAO_FORA_DA_FROTA", "NAO_TRANSPORTADA_SEM_MOTORISTA_VEICULO", "EM_CARGA_DE_OUTRO_MES",
            "BONIFICACAO_TRANSPORTADA", "TRANSPORTADA_FORA_DA_VENDA", "NOTA_DE_OUTRO_MES_NA_CARGA");

    private static final List<String> ORDEM_PONTE2 = List.of(
            "CARGA_NAO_SINCRONIZADA", "DATA_DIFERENTE_NO_FROTAPRO", "NOTAS_NOVAS_NAO_REFLETIDAS",
            "NOTAS_MOVIDAS_AINDA_NA_CARGA", "NAO_TRANSPORTADA_SOMADA_CANCELADA", "NAO_TRANSPORTADA_SOMADA_SEM_MDFE",
            "NAO_TRANSPORTADA_SOMADA_CAMINHAO_FORA_DA_FROTA", "DIMINUICAO_TRAVADA",
            "NAO_TRANSPORTADA_SOMADA_SEM_MOTORISTA_VEICULO", "NAO_TRANSPORTADA_SOMADA_SEM_PEDIDO",
            "NAO_TRANSPORTADA_SOMADA_CARREGAMENTO_INEXISTENTE", "NAO_TRANSPORTADA_SOMADA_SEM_CARREGAMENTO",
            VALOR_DESATUALIZADO, "CARGA_SUMIDA_DO_WINTHOR", "CARGA_SEM_TRANSPORTE_HOJE", "CARGA_DE_TERCEIRO",
            "CARGA_DESCARTADA_NO_SYNC", "DATA_DIFERENTE_NO_WINTHOR");

    private static final Map<String, String[]> TEXTOS = new HashMap<>();

    static {
        TEXTOS.put("NAO_TRANSPORTADA_", new String[]{"Venda que não foi transportada",
                "Notas da venda faturada que não entram no transporte. Veja o motivo de cada uma na lista de notas fora do transporte."});
        TEXTOS.put("NAO_TRANSPORTADA_SEM_CARREGAMENTO", new String[]{"Venda sem carregamento",
                "Notas sem carregamento (balcão, venda direta, retirada): não passam pela frota, não entram."});
        TEXTOS.put("NAO_TRANSPORTADA_CARREGAMENTO_INEXISTENTE", new String[]{"Venda em carregamento inexistente",
                "A nota aponta para um carregamento que não existe mais no WinThor."});
        TEXTOS.put("NAO_TRANSPORTADA_SEM_PEDIDO", new String[]{"Venda em carregamento sem pedido",
                "Carregamento sem pedido no WinThor: a integradora não cria a carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SEM_MDFE", new String[]{"Venda sem manifesto (MDF-e)",
                "Nota com carregamento, mas sem MDF-e: não há prova de transporte, não entra."});
        TEXTOS.put("NAO_TRANSPORTADA_CAMINHAO_FORA_DA_FROTA", new String[]{"Venda transportada por caminhão de terceiro",
                "O manifesto é de caminhão que não é da frota (ex.: TV10 dos clientes de transporte terceirizado): não é transporte da frota e não entra."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_CAMINHAO_FORA_DA_FROTA", new String[]{"Notas de caminhão de terceiro somadas na carga",
                "Notas de manifesto com caminhão que não é da frota que o FrotaPRO soma no valor da carga: não entram no transportado."});
        TEXTOS.put("DIMINUICAO_TRAVADA", new String[]{"Diminuição travada pelo bloqueio",
                "O WinThor já diminuiu a carga, mas o bloqueio de diminuição (transferência/devolução sem motivo reconhecido) mantém o valor maior. É o quanto ia diminuir."});
        TEXTOS.put("CARGA_DE_TERCEIRO", new String[]{"Cargas de caminhão de terceiro",
                "O FrotaPRO tem a carga, mas o caminhão do manifesto não é da frota: não entra no transportado."});
        TEXTOS.put("CARGA_DESCARTADA_NO_SYNC", new String[]{"Cargas que o sync descartou",
                "A carga existe no WinThor, mas o sync não a trouxe porque o motorista ou o caminhão não está cadastrado no FrotaPRO."});
        TEXTOS.put("NAO_TRANSPORTADA_SEM_MOTORISTA_VEICULO", new String[]{"Venda sem motorista ou veículo",
                "Nota sem motorista ou veículo informado: a integradora não cria a carga."});
        TEXTOS.put("EM_CARGA_DE_OUTRO_MES", new String[]{"Venda do mês em carga de outro mês",
                "A nota é do mês, mas a carga dela tem a última nota em outro mês, então o valor conta lá."});
        TEXTOS.put("BONIFICACAO_TRANSPORTADA", new String[]{"Bonificação transportada",
                "Notas de bonificação (valor zerado na venda faturada) que foram transportadas e contam como mercadoria."});
        TEXTOS.put("TRANSPORTADA_FORA_DA_VENDA", new String[]{"Transportada, mas fora da venda faturada",
                "Notas do mês que a rotina 111 não conta (transferência, cliente da própria empresa, outra espécie) mas que foram transportadas."});
        TEXTOS.put("NOTA_DE_OUTRO_MES_NA_CARGA", new String[]{"Notas de outros meses dentro das cargas",
                "Notas emitidas em outro mês que viajam na mesma carga: o valor da carga é a soma de todas as notas dela."});
        TEXTOS.put("CARGA_NAO_SINCRONIZADA", new String[]{"Cargas que ainda não chegaram ao FrotaPRO",
                "Carga com MDF-e no WinThor que não existe no FrotaPRO."});
        TEXTOS.put("DATA_DIFERENTE_NO_FROTAPRO", new String[]{"Cargas datadas em outro mês no FrotaPRO",
                "A carga é do mês no WinThor, mas está com data de outro mês no FrotaPRO."});
        TEXTOS.put("NOTAS_NOVAS_NAO_REFLETIDAS", new String[]{"Notas novas ainda não refletidas",
                "Notas que entraram na carga no WinThor (aumento) e o FrotaPRO ainda não registrou."});
        TEXTOS.put("NOTAS_MOVIDAS_AINDA_NA_CARGA", new String[]{"Notas movidas para outra carga, ainda contadas aqui",
                "Transferência de pedido: o WinThor já tirou a nota desta carga (diminuição) e o FrotaPRO ainda soma."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_", new String[]{"Notas somadas que não são transporte",
                "O valor da carga no FrotaPRO soma todas as notas do carregamento, inclusive as que não são transporte."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_CANCELADA", new String[]{"Notas canceladas somadas na carga",
                "Notas canceladas que o FrotaPRO soma no valor da carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_SEM_MDFE", new String[]{"Notas sem manifesto somadas na carga",
                "Notas do carregamento sem MDF-e que o FrotaPRO soma no valor da carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_SEM_MOTORISTA_VEICULO", new String[]{"Notas sem motorista/veículo somadas na carga",
                "Notas do carregamento sem motorista ou veículo que o FrotaPRO soma no valor da carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_SEM_PEDIDO", new String[]{"Notas sem pedido somadas na carga",
                "Notas de carregamento sem pedido que o FrotaPRO soma no valor da carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_CARREGAMENTO_INEXISTENTE", new String[]{"Notas de carregamento inexistente somadas na carga",
                "Notas cujo carregamento não existe que o FrotaPRO soma no valor da carga."});
        TEXTOS.put("NAO_TRANSPORTADA_SOMADA_SEM_CARREGAMENTO", new String[]{"Notas sem carregamento somadas na carga",
                "Notas sem carregamento que o FrotaPRO soma no valor da carga."});
        TEXTOS.put(VALOR_DESATUALIZADO, new String[]{"Valor gravado diferente da soma das notas",
                "Parte da diferença que não dá para ligar a uma nota: o valor gravado na carga está desatualizado em relação ao WinThor."});
        TEXTOS.put("CARGA_SUMIDA_DO_WINTHOR", new String[]{"Cargas que sumiram do WinThor",
                "O carregamento deixou de existir (pedido movido ou minuta refeita). Se as notas estão em outra carga, o valor está contado duas vezes."});
        TEXTOS.put("CARGA_SEM_TRANSPORTE_HOJE", new String[]{"Cargas sem nenhuma nota transportada hoje",
                "Carga que o FrotaPRO tem, mas em que nenhuma nota é transporte no WinThor hoje."});
        TEXTOS.put("DATA_DIFERENTE_NO_WINTHOR", new String[]{"Cargas do mês no FrotaPRO, de outro mês no WinThor",
                "A carga está datada neste mês no FrotaPRO, mas a última nota dela no WinThor é de outro mês."});
    }
}
