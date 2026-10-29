package br.com.frotasPro.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * Manda o segredo combinado (INTEGRADORA_API_TOKEN) no cabeçalho X-Integradora-Token em toda
 * chamada à frota-pro-integradora, que só atende quem o conhece. O cabeçalho vai SÓ pra
 * integradora: as outras chamadas (BrasilAPI, Resend, Metabase) não recebem o segredo.
 */
@Configuration
public class IntegradoraTokenConfig {

    static final String HEADER = "X-Integradora-Token";

    @Bean
    public RestTemplateCustomizer integradoraTokenCustomizer(
            @Value("${frotapro.integracao.integradora-base-url:http://localhost:8081}") String baseUrl,
            @Value("${frotapro.integracao.integradora-token:}") String token
    ) {
        if (token == null || token.isBlank()) {
            return restTemplate -> { };
        }
        URI integradora = URI.create(baseUrl.trim());
        String valor = token.trim();

        return restTemplate -> restTemplate.getInterceptors().add((request, body, execution) -> {
            URI destino = request.getURI();
            if (mesmoServidor(integradora, destino)) {
                request.getHeaders().set(HEADER, valor);
            }
            return execution.execute(request, body);
        });
    }

    static boolean mesmoServidor(URI base, URI destino) {
        return base.getHost() != null
                && base.getHost().equalsIgnoreCase(destino.getHost())
                && porta(base) == porta(destino);
    }

    private static int porta(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
