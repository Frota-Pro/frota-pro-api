package br.com.frotasPro.api.config;

import br.com.frotasPro.api.util.ClientIp;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Proxies cujos cabeçalhos X-Forwarded-For/X-Real-IP valem (ver {@link ClientIp}). O padrão são as
 * faixas privadas e loopback (nginx na mesma máquina ou na rede do Docker). Se houver um proxy de
 * IP público na frente (ex.: Cloudflare), liste-o em TRUSTED_PROXIES, separado por vírgula.
 */
@Configuration
public class ClientIpConfig {

    @Value("${frotapro.security.trusted-proxies:}")
    private String proxiesConfigurados;

    @PostConstruct
    void aplicar() {
        if (proxiesConfigurados == null || proxiesConfigurados.isBlank()) {
            return;
        }
        List<String> cidrs = Arrays.stream(proxiesConfigurados.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        ClientIp.configurarProxiesDeConfianca(cidrs);
    }
}
