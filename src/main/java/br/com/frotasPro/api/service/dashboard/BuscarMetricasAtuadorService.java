package br.com.frotasPro.api.service.dashboard;

import br.com.frotasPro.api.controller.response.MetricasAtuadorResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lê métricas já coletadas pelo Spring Actuator/Micrometer (em memória, sem
 * chamada HTTP) pra exibir na tela de Saúde do Sistema.
 */
@Service
@RequiredArgsConstructor
public class BuscarMetricasAtuadorService {

    private final MeterRegistry meterRegistry;
    private final HealthEndpoint healthEndpoint;

    public MetricasAtuadorResponse buscar() {
        HealthComponent health = healthEndpoint.health();

        Map<String, String> statusComponentes = new LinkedHashMap<>();
        if (health instanceof CompositeHealth composite) {
            composite.getComponents().forEach((nome, componente) ->
                    statusComponentes.put(nome, componente.getStatus().getCode()));
        }

        return MetricasAtuadorResponse.builder()
                .statusGeral(health.getStatus().getCode())
                .statusComponentes(statusComponentes)
                .uptimeSegundos(gaugeValor("process.uptime"))
                .memoriaUsadaMb(bytesParaMb(somarGauges("jvm.memory.used", "area", "heap")))
                .memoriaMaximaMb(bytesParaMb(somarGauges("jvm.memory.max", "area", "heap")))
                .cpuUsoPercentual(percentual(gaugeValor("system.cpu.usage")))
                .conexoesBancoAtivas(primeiroGaugeInt("hikaricp.connections.active"))
                .conexoesBancoMaximas(primeiroGaugeInt("hikaricp.connections.max"))
                .totalRequisicoesHttp(somarContadorTimer("http.server.requests"))
                .build();
    }

    private Double gaugeValor(String nome) {
        return meterRegistry.find(nome).gauges().stream()
                .findFirst()
                .map(Gauge::value)
                .orElse(null);
    }

    private Double somarGauges(String nome, String tagChave, String tagValor) {
        var gauges = meterRegistry.find(nome).tag(tagChave, tagValor).gauges();
        if (gauges.isEmpty()) return null;
        return gauges.stream().mapToDouble(Gauge::value).sum();
    }

    private Integer primeiroGaugeInt(String nome) {
        return meterRegistry.find(nome).gauges().stream()
                .findFirst()
                .map(g -> (int) Math.round(g.value()))
                .orElse(null);
    }

    private long somarContadorTimer(String nome) {
        return meterRegistry.find(nome).timers().stream()
                .mapToLong(Timer::count)
                .sum();
    }

    private Double bytesParaMb(Double bytes) {
        if (bytes == null) return null;
        return Math.round((bytes / (1024.0 * 1024.0)) * 100.0) / 100.0;
    }

    private BigDecimal percentual(Double fracao) {
        if (fracao == null || fracao < 0) return null;
        return BigDecimal.valueOf(fracao * 100).setScale(1, RoundingMode.HALF_UP);
    }
}
