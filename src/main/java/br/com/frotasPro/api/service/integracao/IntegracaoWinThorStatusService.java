package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.controller.integracao.dto.IntegracaoWinThorStatusResponse;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import br.com.frotasPro.api.service.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IntegracaoWinThorStatusService {

    private static final Duration INTERVALO_MINIMO_ALERTA_DOWN = Duration.ofMinutes(30);

    private final RestTemplateBuilder restTemplateBuilder;
    private final NotificacaoService notificacaoService;

    private volatile boolean integradoraEmFalha = false;
    private volatile LocalDateTime ultimaNotificacaoFalhaEm;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    @Value("${frotapro.integracao.timeout-ms:2000}")
    private long timeoutMs;

    public IntegracaoWinThorStatusResponse verificar() {
        long start = System.currentTimeMillis();

        boolean integradoraOk = false;
        boolean oracleOk = false;
        String integradoraStatus = "DOWN";
        String oracleStatus = "UNKNOWN";

        try {
            RestTemplate rt = restTemplateBuilder
                    .setConnectTimeout(Duration.ofMillis(timeoutMs))
                    .setReadTimeout(Duration.ofMillis(timeoutMs))
                    .build();

            String url = normalizeBaseUrl(integradoraBaseUrl) + "/actuator/health";
            ResponseEntity<Map> resp = rt.getForEntity(url, Map.class);

            Map body = resp.getBody();
            if (resp.getStatusCode().is2xxSuccessful() && body != null) {
                Object status = body.get("status");
                integradoraStatus = status != null ? status.toString() : "UNKNOWN";
                integradoraOk = "UP".equalsIgnoreCase(integradoraStatus);

                Object componentsObj = body.get("components");
                if (componentsObj instanceof Map<?, ?> components) {
                    Object dbObj = components.get("db");
                    if (dbObj instanceof Map<?, ?> dbMap) {
                        Object dbStatus = dbMap.get("status");
                        oracleStatus = dbStatus != null ? dbStatus.toString() : "UNKNOWN";
                        oracleOk = "UP".equalsIgnoreCase(oracleStatus);
                    }
                }
            }
        } catch (RestClientException ex) {
            integradoraOk = false;
            oracleOk = false;
            integradoraStatus = "DOWN";
            oracleStatus = "UNKNOWN";
        }

        long latency = System.currentTimeMillis() - start;

        processarNotificacaoStatusIntegradora(integradoraOk, oracleStatus, latency);

        return IntegracaoWinThorStatusResponse.builder()
                .apiOk(true)
                .integradoraOk(integradoraOk)
                .oracleOk(oracleOk)
                .integradoraStatus(integradoraStatus)
                .oracleStatus(oracleStatus)
                .latenciaMs(latency)
                .verificadoEm(OffsetDateTime.now())
                .build();
    }

    private void processarNotificacaoStatusIntegradora(boolean integradoraOk, String oracleStatus, long latenciaMs) {
        LocalDateTime agora = LocalDateTime.now();

        if (!integradoraOk) {
            boolean deveNotificar = !integradoraEmFalha
                    || ultimaNotificacaoFalhaEm == null
                    || Duration.between(ultimaNotificacaoFalhaEm, agora).compareTo(INTERVALO_MINIMO_ALERTA_DOWN) >= 0;

            integradoraEmFalha = true;

            if (deveNotificar) {
                ultimaNotificacaoFalhaEm = agora;
                notificacaoService.notificar(
                        EventoNotificacao.INTEGRADORA_FORA_AR,
                        TipoNotificacao.ERRO,
                        "Integradora WinThor fora do ar",
                        "Falha ao consultar /actuator/health. Oracle=" + oracleStatus + ", latência=" + latenciaMs + "ms.",
                        "INTEGRADORA_WINTHOR",
                        null,
                        "STATUS"
                );
            }
            return;
        }

        if (integradoraEmFalha) {
            integradoraEmFalha = false;
            ultimaNotificacaoFalhaEm = null;
            notificacaoService.notificar(
                    EventoNotificacao.INTEGRADORA_RECUPERADA,
                    TipoNotificacao.SUCESSO,
                    "Integradora WinThor normalizada",
                    "Integradora voltou a responder. Oracle=" + oracleStatus + ", latência=" + latenciaMs + "ms.",
                    "INTEGRADORA_WINTHOR",
                    null,
                    "STATUS"
            );
        }
    }

    private String normalizeBaseUrl(String base) {
        if (base == null) return "";
        if (base.endsWith("/")) return base.substring(0, base.length() - 1);
        return base;
    }
}
