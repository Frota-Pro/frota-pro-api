package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.CargaDiferenca;
import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse.LinhaPonte;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.CargaFp;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.DevolucaoIn;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Extras;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Frota;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.ManifestoIn;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Nota;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Regras;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Resultado;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Transferencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Um cenário de outubro com um exemplo de cada tipo de diferença. Os valores batem com a conta feita à mão:
 * venda 7.650 -> transportado 7.400 -> FrotaPRO 8.170.
 */
class ConferenciaTransportadoCalculoTest {

    private static final YearMonth MES = YearMonth.of(2026, 10);
    private static final Regras REGRAS = new Regras(Set.of(4, 10), Set.of(5, 11), Set.of(3214));

    private final List<Nota> notas = new ArrayList<>();
    private final List<CargaFp> cargas = new ArrayList<>();

    private static BigDecimal v(String valor) {
        return new BigDecimal(valor);
    }

    private static LocalDate dia(int mes, int d) {
        return LocalDate.of(2026, mes, d);
    }

    /** Nota transportada em condições normais (TV1, cliente 10, NF). */
    private void nota(long numero, LocalDate data, Integer car, String valor) {
        notas.add(new Nota(numero, "NF", data, 10, 1, car, v(valor), false, true, true, true, true));
    }

    private void fp(int numCar, LocalDate data, String valor, boolean naoEncontrada, Long... numerosNotas) {
        cargas.add(new CargaFp(numCar, data, v(valor), "FINALIZADA", naoEncontrada, false, Set.of(numerosNotas)));
    }

    @BeforeEach
    void cenario() {
        // 100: igual nos dois lados
        nota(1, dia(10, 5), 100, "1000");
        nota(2, dia(10, 6), 100, "500");
        fp(100, dia(10, 6), "1500", false, 1L, 2L);

        // 200: bonificação (TV5) transportada
        nota(3, dia(10, 7), 200, "800");
        notas.add(new Nota(4, "NF", dia(10, 7), 10, 5, 200, v("50"), false, true, true, true, true));
        fp(200, dia(10, 7), "850", false, 3L, 4L);

        // venda sem carregamento e venda em carregamento sem MDF-e: nunca viram carga
        notas.add(new Nota(5, "NF", dia(10, 8), 10, 1, 0, v("300"), false, true, false, false, false));
        notas.add(new Nota(6, "NF", dia(10, 8), 10, 1, 300, v("200"), false, true, false, true, true));

        // 400: carga com nota de setembro dentro (o valor da carga soma todas)
        nota(7, dia(10, 9), 400, "1000");
        nota(8, dia(9, 30), 400, "400");
        fp(400, dia(10, 9), "1400", false, 7L, 8L);

        // 500: o WinThor já moveu a nota 10 para a carga 600; o FrotaPRO ainda soma nas duas
        nota(9, dia(10, 10), 500, "700");
        nota(10, dia(10, 10), 600, "300");
        nota(11, dia(10, 11), 600, "100");
        fp(500, dia(10, 10), "1000", false, 9L, 10L);
        // 600 (destino da nota movida) não existe no FrotaPRO ainda

        // 700: carga sumida do WinThor; a nota 12 está hoje na 800
        nota(12, dia(10, 12), 800, "250");
        fp(800, dia(10, 12), "250", false, 12L);
        fp(700, dia(10, 12), "250", true, 12L);

        // 900: no WinThor a última nota é de novembro; no FrotaPRO a carga está em outubro
        nota(13, dia(10, 15), 900, "600");
        nota(14, dia(11, 2), 900, "100");
        fp(900, dia(10, 15), "700", false, 13L, 14L);

        // 1000: nota cancelada no mesmo carregamento, somada pelo FrotaPRO
        nota(15, dia(10, 20), 1000, "900");
        notas.add(new Nota(16, "NF", dia(10, 20), 10, 1, 1000, v("100"), true, true, true, true, true));
        fp(1000, dia(10, 20), "1000", false, 15L);

        // 1100: transferência (TV10) transportada; fora da venda faturada
        notas.add(new Nota(17, "NF", dia(10, 21), 10, 10, 1100, v("400"), false, true, true, true, true));
        fp(1100, dia(10, 21), "400", false, 17L);

        // cliente da própria empresa, sem carregamento: não é venda nem transporte
        notas.add(new Nota(18, "NF", dia(10, 21), 3214, 1, 0, v("150"), false, true, false, false, false));

        // 1200: valor gravado 20 acima da soma das notas, sem nota que explique
        nota(19, dia(10, 22), 1200, "500");
        fp(1200, dia(10, 22), "520", false, 19L);

        // 1300: o WinThor ganhou uma nota que o FrotaPRO ainda não tem
        nota(20, dia(10, 23), 1300, "300");
        nota(21, dia(10, 23), 1300, "200");
        fp(1300, dia(10, 23), "300", false, 20L);
    }

    private Resultado calcular() {
        return ConferenciaTransportadoCalculo.calcular(MES, notas, cargas, REGRAS);
    }

    private static Optional<LinhaPonte> linha(List<LinhaPonte> linhas, String codigo) {
        return linhas.stream().filter(l -> l.codigo().equals(codigo)).findFirst();
    }

    private static void assertLinha(List<LinhaPonte> linhas, String codigo, String valor) {
        LinhaPonte l = linha(linhas, codigo).orElseThrow(() -> new AssertionError("sem a linha " + codigo));
        assertEquals(0, v(valor).compareTo(l.valor()), codigo + " esperado " + valor + " mas veio " + l.valor());
    }

    @Test
    void totaisEPontesFechamAoCentavo() {
        Resultado r = calcular();

        assertEquals(0, v("7650").compareTo(r.totais().vendaFaturada()));
        assertEquals(16, r.totais().notasVendaFaturada());
        assertEquals(0, v("7400").compareTo(r.totais().transportadoWinThor()));
        assertEquals(0, v("8170").compareTo(r.totais().transportadoFrotaPro()));
        assertEquals(0, v("770").compareTo(r.totais().diferenca()));
        assertTrue(r.fechou(), "as duas pontes têm que fechar");

        BigDecimal ponte1 = r.ponteVendaParaTransportado().stream().map(LinhaPonte::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ponte2 = r.ponteWinThorParaFrotaPro().stream().map(LinhaPonte::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, v("7400").compareTo(v("7650").add(ponte1)));
        assertEquals(0, v("8170").compareTo(v("7400").add(ponte2)));
    }

    @Test
    void ponteDaVendaParaOTransportado() {
        List<LinhaPonte> p = calcular().ponteVendaParaTransportado();

        assertLinha(p, "NAO_TRANSPORTADA_SEM_CARREGAMENTO", "-300");
        assertLinha(p, "NAO_TRANSPORTADA_SEM_MDFE", "-200");
        assertLinha(p, "EM_CARGA_DE_OUTRO_MES", "-600");
        assertLinha(p, "BONIFICACAO_TRANSPORTADA", "50");
        assertLinha(p, "TRANSPORTADA_FORA_DA_VENDA", "400");
        assertLinha(p, "NOTA_DE_OUTRO_MES_NA_CARGA", "400");
        assertEquals(6, p.size(), "só entram linhas com valor");
    }

    @Test
    void ponteDoWinThorParaOFrotaPro() {
        List<LinhaPonte> p = calcular().ponteWinThorParaFrotaPro();

        assertLinha(p, "CARGA_NAO_SINCRONIZADA", "-400");
        assertLinha(p, "NOTAS_MOVIDAS_AINDA_NA_CARGA", "300");
        assertLinha(p, "NAO_TRANSPORTADA_SOMADA_CANCELADA", "100");
        assertLinha(p, "NOTAS_NOVAS_NAO_REFLETIDAS", "-200");
        assertLinha(p, ConferenciaTransportadoCalculo.VALOR_DESATUALIZADO, "20");
        assertLinha(p, "CARGA_SUMIDA_DO_WINTHOR", "250");
        assertLinha(p, "DATA_DIFERENTE_NO_WINTHOR", "700");
        assertEquals(7, p.size());
    }

    @Test
    void cargaIgualNosDoisLadosNaoApareceComoDiferenca() {
        List<Integer> comDiferenca = calcular().cargasComDiferenca().stream().map(CargaDiferenca::numCar).toList();

        assertTrue(!comDiferenca.contains(100) && !comDiferenca.contains(200) && !comDiferenca.contains(400));
        assertTrue(!comDiferenca.contains(800) && !comDiferenca.contains(1100));
    }

    @Test
    void cargaSumidaMostraParaOndeAsNotasForam() {
        CargaDiferenca sumida = calcular().cargasComDiferenca().stream()
                .filter(c -> c.numCar() == 700).findFirst().orElseThrow();

        assertEquals("CARGA_SUMIDA_DO_WINTHOR", sumida.categoria());
        assertEquals(1, sumida.notas().size());
        assertEquals(800, sumida.notas().get(0).paraCarga());
        assertTrue(sumida.explicacao().contains("contado duas vezes"));
    }

    @Test
    void notaMovidaApareceNaCargaDeOrigemComODestino() {
        CargaDiferenca c = calcular().cargasComDiferenca().stream()
                .filter(x -> x.numCar() == 500).findFirst().orElseThrow();

        assertEquals("VALOR_DIFERENTE", c.categoria());
        assertEquals(0, v("300").compareTo(c.diferenca()));
        assertTrue(c.notas().stream().anyMatch(n -> n.numNota() == 10 && n.paraCarga() == 600));
    }

    @Test
    void notasForaDoTransporteTrazemOMotivo() {
        Resultado r = calcular();

        assertTrue(r.notasFora().stream().anyMatch(n -> n.numNota() == 5 && n.motivo().equals("SEM_CARREGAMENTO")));
        assertTrue(r.notasFora().stream().anyMatch(n -> n.numNota() == 6 && n.motivo().equals("SEM_MDFE")));
        assertTrue(r.notasFora().stream().anyMatch(n -> n.numNota() == 16 && n.motivo().equals("CANCELADA")));
        assertEquals(4, r.notasForaTotal(), "5, 6, 16 e 18 (cliente da empresa) estão fora do transporte");
    }

    @Test
    void mesSemNadaNaoQuebra() {
        Resultado r = ConferenciaTransportadoCalculo.calcular(YearMonth.of(2026, 1), List.of(), List.of(), REGRAS);

        assertEquals(0, BigDecimal.ZERO.compareTo(r.totais().diferenca()));
        assertTrue(r.fechou());
        assertTrue(r.ponteVendaParaTransportado().isEmpty() && r.ponteWinThorParaFrotaPro().isEmpty());
    }

    @Test
    void notaSemTvOuSemClienteNaoQuebraARegraDaVenda() {
        notas.add(new Nota(99, "NF", dia(10, 25), null, null, 0, v("10"), false, true, false, false, false));

        Resultado r = calcular();

        assertEquals(0, v("7660").compareTo(r.totais().vendaFaturada()));
        assertTrue(r.fechou());
    }

    // ------------------------------------------------------------------ frota x terceiro, pequenas, manifestos, devoluções

    private static final Frota FROTA = new Frota(Set.of("10"), Set.of("1"));

    private static Extras extras() {
        return new Extras(List.of(), FROTA, Map.of(), List.of(), List.of(), BigDecimal.valueOf(500));
    }

    private Resultado calcularComFrota(Extras extras) {
        return ConferenciaTransportadoCalculo.calcular(MES, notas, cargas, REGRAS, extras);
    }

    /** Nota TV10 num manifesto cujo motorista/caminhão são os informados (códigos do WinThor). */
    private void notaNoManifesto(long numero, Integer car, String valor, int tv, Integer codMotorista, String codVeiculo) {
        notas.add(new Nota(numero, "NF", dia(10, 24), 15575, tv, car, v(valor), false, true, true, true, true,
                codMotorista, codVeiculo, false, false));
    }

    @Test
    void caminhaoDeTerceiroNaoEntraComoTransportadoENotaFicaFora() {
        notaNoManifesto(30, 1400, "600", 10, 999, "99");

        Resultado r = calcularComFrota(extras());

        assertTrue(r.notasFora().stream().anyMatch(n -> n.numNota() == 30 && n.motivo().equals("CAMINHAO_FORA_DA_FROTA")));
        assertEquals(0, v("7400").compareTo(r.totais().transportadoWinThor()), "o terceiro não soma no transportado");
        assertTrue(r.fechou());
    }

    @Test
    void tv10ComManifestoDaFrotaEntraComoTransportado() {
        notaNoManifesto(30, 1400, "600", 10, 1, "10");

        Resultado r = calcularComFrota(extras());

        assertEquals(0, v("8000").compareTo(r.totais().transportadoWinThor()), "TV10 com motorista e caminhão da frota entra");
        assertLinha(r.ponteVendaParaTransportado(), "TRANSPORTADA_FORA_DA_VENDA", "1000");
        assertTrue(r.fechou());
    }

    @Test
    void cargaDeTerceiroQueOFrotaProTemTemCategoriaPropria() {
        notaNoManifesto(30, 1400, "600", 10, 999, "99");
        fp(1400, dia(10, 24), "600", false, 30L);

        Resultado r = calcularComFrota(extras());

        CargaDiferenca c = r.cargasComDiferenca().stream().filter(x -> x.numCar() == 1400).findFirst().orElseThrow();
        assertEquals("CARGA_DE_TERCEIRO", c.categoria());
        assertLinha(r.ponteWinThorParaFrotaPro(), "CARGA_DE_TERCEIRO", "600");
        assertTrue(r.fechou());
    }

    @Test
    void motoristaForaDaFrotaComCaminhaoNossoEntraEViraLancamentoManual() {
        notaNoManifesto(31, 1500, "800", 1, 777, "10");
        fp(1500, dia(10, 24), "800", false, 31L);

        Resultado r = calcularComFrota(extras());

        assertEquals(0, v("8200").compareTo(r.totais().transportadoWinThor()));
        var manual = r.lancamentoManual().stream().filter(x -> x.numCar() == 1500).findFirst().orElseThrow();
        assertEquals("777", manual.codMotorista());
        assertTrue(manual.cargaNoFrotaPro());
        assertTrue(manual.explicacao().contains("km"));
        assertTrue(r.fechou());
    }

    @Test
    void lancamentoManualAvisaQuandoACargaNemChegouAoFrotaPro() {
        notaNoManifesto(31, 1500, "800", 1, 777, "10");

        var manual = calcularComFrota(extras()).lancamentoManual().stream().filter(x -> x.numCar() == 1500).findFirst().orElseThrow();

        assertTrue(!manual.cargaNoFrotaPro());
        assertTrue(manual.explicacao().contains("NÃO está no FrotaPRO"));
    }

    @Test
    void cargaDescartadaNoSyncTemCategoriaPropriaComOMotivo() {
        notaNoManifesto(32, 1600, "500", 1, 777, "10");
        var comDescartada = new Extras(List.of(), FROTA, Map.of(1600, "Motorista não cadastrado no FrotaPRO"),
                List.of(), List.of(), BigDecimal.valueOf(500));

        Resultado r = calcularComFrota(comDescartada);

        CargaDiferenca c = r.cargasComDiferenca().stream().filter(x -> x.numCar() == 1600).findFirst().orElseThrow();
        assertEquals("CARGA_DESCARTADA_NO_SYNC", c.categoria());
        assertTrue(c.explicacao().contains("Motorista não cadastrado"));
        assertLinha(r.ponteWinThorParaFrotaPro(), "CARGA_DESCARTADA_NO_SYNC", "-500");
    }

    @Test
    void cargaPequenaDeReentregaDizDeOndeVeioEOQueConferir() {
        nota(40, dia(10, 25), 1700, "188.68");
        cargas.add(new CargaFp(1700, dia(10, 25), v("188.68"), "SINCRONIZADA", false, false, Set.of(40L),
                v("26.392"), "FRANCISCO GOMES", "TOT9B66", false, "REENTREGA", "417085", v("188.68"), false));

        Resultado r = calcularComFrota(extras());

        assertEquals(1, r.cargasPequenas().size(), "só a carga abaixo de 500 kg aparece");
        var c = r.cargasPequenas().get(0);
        assertEquals("REENTREGA", c.origem());
        assertEquals("417085", c.origemDe());
        assertTrue(c.explicacao().contains("417085"));
        assertEquals("TOT9B66", c.placa());
    }

    @Test
    void cargaSemNotaCujasNotasForamTodasParaOutraCargaPodeSerZeradaSoComALogConfirmando() {
        nota(50, dia(10, 12), 1900, "250");
        fp(1900, dia(10, 12), "250", false, 50L);
        cargas.add(new CargaFp(1800, dia(10, 12), v("250"), "SINCRONIZADA", true, false, Set.of(50L)));

        var semLog = calcularComFrota(extras()).cargasComDiferenca().stream().filter(x -> x.numCar() == 1800).findFirst().orElseThrow();
        var comLog = ConferenciaTransportadoCalculo.calcular(MES, notas, cargas, REGRAS,
                extras().comTransferencias(List.of(new Transferencia(50, 1800, 1900, LocalDateTime.of(2026, 10, 12, 9, 0), "PEDIDO PARA REENTREGA"))))
                .cargasComDiferenca().stream().filter(x -> x.numCar() == 1800).findFirst().orElseThrow();

        assertTrue(!semLog.podeSerZerada(), "sem o log não dá para ter certeza de que as notas foram transferidas");
        assertTrue(comLog.podeSerZerada());
    }

    @Test
    void cargaIniciadaNuncaPodeSerZerada() {
        nota(50, dia(10, 12), 1900, "250");
        fp(1900, dia(10, 12), "250", false, 50L);
        cargas.add(new CargaFp(1800, dia(10, 12), v("250"), "EM_ROTA", true, false, Set.of(50L)));

        var c = ConferenciaTransportadoCalculo.calcular(MES, notas, cargas, REGRAS,
                extras().comTransferencias(List.of(new Transferencia(50, 1800, 1900, LocalDateTime.of(2026, 10, 12, 9, 0), "X"))))
                .cargasComDiferenca().stream().filter(x -> x.numCar() == 1800).findFirst().orElseThrow();

        assertTrue(!c.podeSerZerada());
    }

    @Test
    void cargaJaZeradaNaoApareceMaisComoDiferenca() {
        nota(50, dia(10, 12), 1900, "250");
        fp(1900, dia(10, 12), "250", false, 50L);
        cargas.add(new CargaFp(1800, dia(10, 12), BigDecimal.ZERO, "SINCRONIZADA", true, false, Set.of(50L),
                BigDecimal.ZERO, null, null, false, null, null, null, true));

        Resultado r = calcularComFrota(extras());

        assertTrue(r.cargasComDiferenca().stream().noneMatch(x -> x.numCar() == 1800));
        assertTrue(r.fechou());
    }

    @Test
    void diminuicaoTravadaPeloBloqueioViraLinhaPropriaEnaoValorDesatualizado() {
        nota(60, dia(10, 26), 2000, "700");
        cargas.add(new CargaFp(2000, dia(10, 26), v("1000"), "FINALIZADA", false, true, Set.of(60L),
                null, null, null, false, null, null, v("700"), false));

        Resultado r = calcularComFrota(extras());

        assertLinha(r.ponteWinThorParaFrotaPro(), "DIMINUICAO_TRAVADA", "300");
        assertLinha(r.ponteWinThorParaFrotaPro(), ConferenciaTransportadoCalculo.VALOR_DESATUALIZADO, "20"); // os 20 da carga 1200 do cenario; a travada nao entra aqui
        assertTrue(r.fechou());
    }

    @Test
    void devolucaoEInformativaENaoMudaOValorTransportado() {
        var comDevolucao = new Extras(List.of(), FROTA, Map.of(), List.of(),
                List.of(new DevolucaoIn(1, v("100.50"), v("5")), new DevolucaoIn(3, v("20"), v("2.5"))), BigDecimal.valueOf(500));

        Resultado r = calcularComFrota(comDevolucao);

        assertEquals(2, r.resumoDevolucoes().notas());
        assertEquals(0, v("120.50").compareTo(r.resumoDevolucoes().valor()));
        assertEquals(0, v("7.5").compareTo(r.resumoDevolucoes().pesoKg()));
        assertEquals(0, v("7400").compareTo(r.totais().transportadoWinThor()), "a devolução não diminui o valor da nota");
        assertEquals(100, r.devolucoes().get(0).numCar());
    }

    @Test
    void manifestoSemNotaComCaminhaoDaFrotaContaComoViagemSemValor() {
        var manifestos = List.of(
                new ManifestoIn(1L, 1, "10", "100", dia(10, 3), v("12000")),
                new ManifestoIn(2L, 555, "10", "100", dia(10, 4), v("8000")),
                new ManifestoIn(3L, 1, "99", "100", dia(10, 5), v("3000")));
        var comManifestos = new Extras(List.of(), FROTA, Map.of(), manifestos, List.of(), BigDecimal.valueOf(500));

        var lista = calcularComFrota(comManifestos).manifestosSemNota();

        assertEquals(3, lista.size());
        assertTrue(lista.get(0).caminhaoDaFrota() && lista.get(1).caminhaoDaFrota() && !lista.get(2).caminhaoDaFrota(), "os da frota vêm primeiro");
        assertTrue(lista.get(1).explicacao().contains("Motorista fora da frota"));
        assertTrue(lista.get(2).explicacao().contains("não é da frota"));
    }

    // ------------------------------------------------------------------ transferência entre cargas

    private static Transferencia transf(long nota, int de, int para, LocalDateTime quando) {
        return new Transferencia(nota, de, para, quando, "TRANSF. DE PEDIDO ENTRE CARGAS");
    }

    @Test
    void historicoDaTransferenciaApareceNaNotaMovidaEExplicaOQueSaiuEOQueEntrou() {
        List<Transferencia> log = List.of(
                transf(10, 500, 600, LocalDateTime.of(2026, 10, 10, 14, 0)),
                transf(12, 700, 800, LocalDateTime.of(2026, 10, 12, 9, 30)),
                transf(7, 111, 400, LocalDateTime.of(2026, 9, 20, 8, 0)));

        Resultado r = ConferenciaTransportadoCalculo.calcular(MES, notas, log, cargas, REGRAS);

        CargaDiferenca origem = r.cargasComDiferenca().stream().filter(x -> x.numCar() == 500).findFirst().orElseThrow();
        var nota10 = origem.notas().stream().filter(n -> n.numNota() == 10).findFirst().orElseThrow();
        assertEquals(1, nota10.historico().size());
        assertEquals(500, nota10.historico().get(0).deCarga());
        assertEquals(600, nota10.historico().get(0).paraCarga());
        assertTrue(origem.explicacao().contains("1 com transferência registrada"), origem.explicacao());

        CargaDiferenca sumida = r.cargasComDiferenca().stream().filter(x -> x.numCar() == 700).findFirst().orElseThrow();
        assertTrue(sumida.explicacao().contains("1 com transferência registrada no log"), sumida.explicacao());
        assertTrue(r.fechou());
    }

    @Test
    void movimentosEntreCargasSaiDeUmaEEntraNaOutraComOMesmoValor() {
        List<Transferencia> log = List.of(
                transf(10, 500, 600, LocalDateTime.of(2026, 10, 10, 14, 0)),
                transf(12, 700, 800, LocalDateTime.of(2026, 10, 12, 9, 30)),
                transf(7, 111, 400, LocalDateTime.of(2026, 9, 20, 8, 0)));

        Resultado r = ConferenciaTransportadoCalculo.calcular(MES, notas, log, cargas, REGRAS);

        assertEquals(2, r.movimentos().size(), "a transferência de setembro não é do mês");
        var m = r.movimentos().stream().filter(x -> x.deCarga() == 500).findFirst().orElseThrow();
        assertEquals(600, m.paraCarga());
        assertEquals(0, v("300").compareTo(m.valor()));
        assertEquals(2, r.resumoMovimentos().transferencias());
        assertEquals(0, v("550").compareTo(r.resumoMovimentos().valor()));
    }

    @Test
    void transferenciaDeDepoisDoMesTambemEhMostrada() {
        List<Transferencia> log = List.of(transf(10, 500, 600, LocalDateTime.of(2026, 11, 3, 10, 0)));

        Resultado r = ConferenciaTransportadoCalculo.calcular(MES, notas, log, cargas, REGRAS);

        assertEquals(1, r.movimentos().size());
        CargaDiferenca origem = r.cargasComDiferenca().stream().filter(x -> x.numCar() == 500).findFirst().orElseThrow();
        assertEquals(LocalDateTime.of(2026, 11, 3, 10, 0),
                origem.notas().stream().filter(n -> n.numNota() == 10).findFirst().orElseThrow().historico().get(0).quando());
    }
}
