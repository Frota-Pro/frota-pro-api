package br.com.frotasPro.api.service.integracao;

import br.com.frotasPro.api.controller.integracao.dto.IntegracaoWinThorLogsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IntegracaoWinThorLogsService {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${frotapro.logs.api-file:logs/frotapro-api.log}")
    private String apiLogFile;

    @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}")
    private String integradoraBaseUrl;

    @Value("${frotapro.logs.integradora-logfile-path:/actuator/logfile}")
    private String integradoraLogfilePath;

    @Value("${frotapro.integracao.timeout-ms:2000}")
    private long timeoutMs;

    public IntegracaoWinThorLogsResponse buscarLogs(String source, int lines) {
        String src = (source == null ? "API" : source.trim().toUpperCase());
        int n = clamp(lines, 10, 2000);

        List<String> out;
        if ("INTEGRADORA".equals(src)) {
            out = tailIntegradora(n);
        } else {
            src = "API";
            out = tailFile(apiLogFile, n);
        }

        return IntegracaoWinThorLogsResponse.builder()
                .source(src)
                .fetchedAt(OffsetDateTime.now())
                .linesRequested(n)
                .linesReturned(out.size())
                .lines(out)
                .build();
    }

    private List<String> tailFile(String filePath, int n) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return List.of("[LOG] Arquivo não encontrado: " + path.toAbsolutePath());
        }

        // Tail simples (memória): lê tudo e pega as últimas N linhas.
        // Para logs muito grandes, depois otimizamos (RandomAccessFile).
        Deque<String> deque = new ArrayDeque<>(n);

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (deque.size() == n) deque.removeFirst();
                deque.addLast(line);
            }
        } catch (IOException e) {
            return List.of("[LOG] Erro ao ler arquivo: " + e.getMessage());
        }

        return new ArrayList<>(deque);
    }

    @SuppressWarnings("rawtypes")
    private List<String> tailIntegradora(int n) {
        try {
            RestTemplate rt = restTemplateBuilder
                    .setConnectTimeout(Duration.ofMillis(timeoutMs))
                    .setReadTimeout(Duration.ofMillis(timeoutMs))
                    .build();

            String base = normalizeBaseUrl(integradoraBaseUrl);
            String url = base + integradoraLogfilePath;

            // Actuator logfile retorna texto (conteúdo do arquivo).
            ResponseEntity<String> resp = rt.getForEntity(url, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return List.of("[LOG] Falha ao obter logs da integradora: HTTP " + resp.getStatusCode());
            }

            String body = resp.getBody();
            String[] all = body.split("\\r?\\n");
            int start = Math.max(0, all.length - n);
            List<String> tail = new ArrayList<>();
            for (int i = start; i < all.length; i++) tail.add(all[i]);
            return tail;

        } catch (RestClientException ex) {
            return List.of("[LOG] Integradora indisponível para logs: " + ex.getMessage());
        }
    }

    private int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private String normalizeBaseUrl(String base) {
        if (base == null) return "";
        if (base.endsWith("/")) return base.substring(0, base.length() - 1);
        return base;
    }
}
