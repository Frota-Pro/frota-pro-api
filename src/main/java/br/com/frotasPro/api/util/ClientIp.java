package br.com.frotasPro.api.util;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * IP real de quem chamou, sem se deixar enganar por cabeçalhos forjados.
 *
 * X-Forwarded-For e X-Real-IP são texto que o próprio cliente pode escrever, então só valem
 * quando a conexão chega de um proxy NOSSO (nginx, rede do Docker: faixas privadas/loopback,
 * configuráveis em frotapro.security.trusted-proxies). Nesse caso lê-se X-Forwarded-For da
 * direita para a esquerda, pulando os proxies de confiança: o primeiro endereço "de fora" é o
 * cliente. O que o cliente escreveu à esquerda é ignorado. Conexão que não vem de proxy nosso
 * usa o endereço da própria conexão.
 *
 * O ForwardedHeaderFilter do Spring (forward-headers-strategy: framework) troca getRemoteAddr()
 * pelo primeiro valor do cabeçalho (forjável) e esconde o X-Forwarded-For; por isso aqui se
 * desembrulha a requisição até a original.
 */
public final class ClientIp {

    public static final List<String> PROXIES_PADRAO = List.of(
            "127.0.0.0/8", "::1/128", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16", "fc00::/7");

    private static final Pattern FORMATO_IP = Pattern.compile("[0-9a-fA-F:.]{2,45}");

    private static volatile List<IpAddressMatcher> proxiesDeConfianca = construir(PROXIES_PADRAO);

    private ClientIp() {
    }

    /** Troca a lista de proxies de confiança (itens em CIDR ou IP puro). Lista vazia = nenhum proxy é confiável. */
    public static void configurarProxiesDeConfianca(List<String> cidrs) {
        proxiesDeConfianca = construir(cidrs == null ? PROXIES_PADRAO : cidrs);
    }

    public static String de(HttpServletRequest requisicao) {
        HttpServletRequest original = desembrulhar(requisicao);

        String conexao = valido(original.getRemoteAddr());
        if (conexao == null) {
            return "unknown";
        }
        if (!ehProxyDeConfianca(conexao)) {
            return conexao;
        }

        String encaminhados = original.getHeader("X-Forwarded-For");
        if (encaminhados != null && !encaminhados.isBlank()) {
            String[] cadeia = encaminhados.split(",");
            for (int i = cadeia.length - 1; i >= 0; i--) {
                String candidato = valido(cadeia[i].trim());
                if (candidato == null) {
                    // entrada quebrada: não dá pra confiar no que vem antes dela
                    return conexao;
                }
                if (!ehProxyDeConfianca(candidato)) {
                    return candidato;
                }
            }
        }

        String real = valido(original.getHeader("X-Real-IP"));
        if (real != null) {
            return real;
        }
        return conexao;
    }

    private static HttpServletRequest desembrulhar(HttpServletRequest requisicao) {
        ServletRequest atual = requisicao;
        while (atual instanceof ServletRequestWrapper embrulho && embrulho.getRequest() instanceof HttpServletRequest interna) {
            atual = interna;
        }
        return (HttpServletRequest) atual;
    }

    private static boolean ehProxyDeConfianca(String ip) {
        for (IpAddressMatcher faixa : proxiesDeConfianca) {
            try {
                if (faixa.matches(ip)) {
                    return true;
                }
            } catch (IllegalArgumentException ignorado) {
                // ip em formato que a faixa não entende (ex.: v6 numa faixa v4): não casa
            }
        }
        return false;
    }

    private static String valido(String ip) {
        if (ip == null) {
            return null;
        }
        String limpo = ip.trim();
        return FORMATO_IP.matcher(limpo).matches() ? limpo : null;
    }

    private static List<IpAddressMatcher> construir(List<String> cidrs) {
        List<IpAddressMatcher> faixas = new ArrayList<>();
        for (String cidr : cidrs) {
            if (cidr != null && !cidr.isBlank()) {
                faixas.add(new IpAddressMatcher(cidr.trim()));
            }
        }
        return List.copyOf(faixas);
    }
}
