package br.com.frotasPro.api.service.conferencia;

import br.com.frotasPro.api.controller.response.ConferenciaTransportadoResponse;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.CargaFp;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Extras;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Nota;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Regras;
import br.com.frotasPro.api.service.conferencia.ConferenciaTransportadoCalculo.Resultado;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Conferência mensal do valor transportado: junta as notas do WinThor (via integradora) com as cargas do
 * FrotaPRO e entrega as pontes explicadas. Só leitura; o resultado fica em memória por pouco tempo porque
 * cada geração consulta o Oracle do WinThor.
 */
@Slf4j
@Service
public class ConferenciaTransportadoService {

    private static final long VALIDADE_MS = 10 * 60 * 1000L;

    private final ConferenciaWinThorClient winThorClient;
    private final CargasFrotaProLeitor cargasLeitor;
    private final CadastroFrotaLeitor cadastroLeitor;
    private final Regras regras;
    private final BigDecimal limitePequenaKg;
    private final Map<YearMonth, Guardado> guardados = new ConcurrentHashMap<>();

    private record Guardado(long criadoEm, ConferenciaTransportadoResponse resposta) {
    }

    public ConferenciaTransportadoService(
            ConferenciaWinThorClient winThorClient,
            CargasFrotaProLeitor cargasLeitor,
            CadastroFrotaLeitor cadastroLeitor,
            // Regras da venda faturada de referência (rotina 111 da empresa). Só servem pra comparar: o valor que vale é o transportado.
            @Value("${frotapro.conferencia.tv-excluidas:4,10}") List<Integer> tvExcluidas,
            @Value("${frotapro.conferencia.tv-zeradas:5,11}") List<Integer> tvZeradas,
            @Value("${frotapro.conferencia.clientes-ignorados:3214}") List<Integer> clientesIgnorados,
            // Carga com peso abaixo disto (kg) entra no relatório de cargas pequenas: caminhão não faz viagem para 100 kg.
            @Value("${frotapro.conferencia.carga-pequena-kg:500}") int limitePequenaKg
    ) {
        this.winThorClient = winThorClient;
        this.cargasLeitor = cargasLeitor;
        this.cadastroLeitor = cadastroLeitor;
        this.regras = new Regras(Set.copyOf(tvExcluidas), Set.copyOf(tvZeradas), Set.copyOf(clientesIgnorados));
        this.limitePequenaKg = BigDecimal.valueOf(limitePequenaKg);
    }

    /** Descarta o resultado guardado do mês (depois de uma ação que muda as cargas, como zerar as transferidas). */
    public void invalidar(YearMonth mes) {
        guardados.remove(mes);
    }

    public ConferenciaTransportadoResponse gerar(YearMonth mes, boolean atualizar) {
        Guardado g = guardados.get(mes);
        if (!atualizar && g != null && System.currentTimeMillis() - g.criadoEm() < VALIDADE_MS) {
            return g.resposta();
        }

        ConferenciaWinThorClient.Dados dados = winThorClient.buscar(mes.atDay(1), mes.atEndOfMonth());
        List<Nota> notas = dados.notas();
        Set<Integer> carregamentos = notas.stream()
                .map(Nota::numCar)
                .filter(c -> c != null && c > 1)
                .collect(Collectors.toSet());
        List<CargaFp> cargas = cargasLeitor.buscar(mes.atDay(1), mes.atEndOfMonth(), carregamentos);

        ConferenciaTransportadoResponse resposta = montar(mes, ConferenciaTransportadoCalculo.calcular(mes, notas, cargas, regras,
                new Extras(dados.transferencias(), cadastroLeitor.frota(), cadastroLeitor.descartadas(),
                        dados.manifestos(), dados.devolucoes(), limitePequenaKg)),
                limitePequenaKg.intValue());
        if (!resposta.fechou()) {
            log.warn("Conferência do transportado de {} não fechou ao centavo: revisar o cálculo.", mes);
        }

        if (guardados.size() > 24) guardados.clear();
        guardados.put(mes, new Guardado(System.currentTimeMillis(), resposta));
        return resposta;
    }

    static ConferenciaTransportadoResponse montar(YearMonth mes, Resultado r, int limitePequenaKg) {
        return new ConferenciaTransportadoResponse(
                mes.toString(),
                OffsetDateTime.now(FusoHorarioUtils.ZONE_BRASIL),
                r.totais(),
                r.ponteVendaParaTransportado(),
                r.ponteWinThorParaFrotaPro(),
                r.cargasComDiferenca(),
                r.notasFora(),
                r.notasForaTotal(),
                r.fechou(),
                r.resumoMovimentos(),
                r.movimentos(),
                limitePequenaKg,
                r.cargasPequenas(),
                r.lancamentoManual(),
                r.manifestosSemNota(),
                r.resumoDevolucoes(),
                r.devolucoes());
    }
}
