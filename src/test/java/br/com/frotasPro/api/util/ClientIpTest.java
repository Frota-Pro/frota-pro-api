package br.com.frotasPro.api.util;

import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClientIpTest {

    @AfterEach
    void restaurarPadrao() {
        ClientIp.configurarProxiesDeConfianca(ClientIp.PROXIES_PADRAO);
    }

    private MockHttpServletRequest requisicao(String conexao, String forwardedFor, String realIp) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr(conexao);
        if (forwardedFor != null) {
            r.addHeader("X-Forwarded-For", forwardedFor);
        }
        if (realIp != null) {
            r.addHeader("X-Real-IP", realIp);
        }
        return r;
    }

    @Test
    void conexaoDireta_naoConfiaEmCabecalhoNenhum() {
        // quem fala direto com a API (sem passar pelo nosso proxy) pode escrever o que quiser nos cabeçalhos
        assertEquals("203.0.113.7", ClientIp.de(requisicao("203.0.113.7", "1.2.3.4", "5.6.7.8")));
    }

    @Test
    void viaProxyNosso_usaOClienteQueOProxyEnxergou() {
        // nginx (172.18.0.1) acrescenta o IP de quem conectou ao final do que o cliente mandou
        assertEquals("198.51.100.20", ClientIp.de(requisicao("172.18.0.1", "198.51.100.20", null)));
    }

    @Test
    void valorForjadoPeloClienteNaEsquerdaEIgnorado() {
        // o cliente mandou "9.9.9.9"; o nginx acrescentou o IP verdadeiro
        assertEquals("198.51.100.20", ClientIp.de(requisicao("172.18.0.1", "9.9.9.9, 198.51.100.20", null)));
        // tentando variar a cada tentativa o resultado continua sendo o verdadeiro
        assertEquals("198.51.100.20", ClientIp.de(requisicao("172.18.0.1", "8.8.8.8, 198.51.100.20", null)));
    }

    @Test
    void pulaOsProxiesDeConfiancaDaCadeia() {
        assertEquals("198.51.100.20",
                ClientIp.de(requisicao("10.0.0.2", "198.51.100.20, 192.168.1.10, 10.0.0.9", null)));
    }

    @Test
    void semXForwardedForUsaOXRealIpDoProxy() {
        assertEquals("198.51.100.20", ClientIp.de(requisicao("127.0.0.1", null, "198.51.100.20")));
    }

    @Test
    void semNadaUsaAConexao() {
        assertEquals("127.0.0.1", ClientIp.de(requisicao("127.0.0.1", null, null)));
        assertEquals("unknown", ClientIp.de(requisicao("", null, null)));
    }

    @Test
    void entradaQuebradaNaCadeiaNaoPermiteConfiarNoQueVemAntes() {
        assertEquals("172.18.0.1", ClientIp.de(requisicao("172.18.0.1", "1.1.1.1, lixo;DROP, 172.18.0.5", null)));
    }

    @Test
    void conteudoEstranhoNaoVirouIpNemEntraEmChaveDeRedis() {
        assertEquals("172.18.0.1", ClientIp.de(requisicao("172.18.0.1", "auth:block:ip:x", null)));
    }

    @Test
    void ipv6Funciona() {
        assertEquals("2001:db8::7", ClientIp.de(requisicao("::1", "2001:db8::7", null)));
        assertEquals("2001:db8::7", ClientIp.de(requisicao("2001:db8::7", "1.2.3.4", null)));
    }

    @Test
    void proxyDeIpPublicoSoVaiSerConfiavelSeForConfigurado() {
        // ex.: Cloudflare na frente do nginx
        MockHttpServletRequest r = requisicao("10.0.0.2", "198.51.100.20, 104.16.0.5", null);
        assertEquals("104.16.0.5", ClientIp.de(r));

        ClientIp.configurarProxiesDeConfianca(List.of("10.0.0.0/8", "104.16.0.0/13"));
        assertEquals("198.51.100.20", ClientIp.de(r));
    }

    @Test
    void enxergaARequisicaoOriginalPorBaixoDosEmbrulhosDoSpring() {
        // o ForwardedHeaderFilter troca getRemoteAddr() pelo primeiro valor do cabeçalho (forjável)
        MockHttpServletRequest original = requisicao("203.0.113.7", "1.2.3.4", null);
        HttpServletRequestWrapper embrulhada = new HttpServletRequestWrapper(original) {
            @Override
            public String getRemoteAddr() {
                return "1.2.3.4";
            }
        };

        assertEquals("203.0.113.7", ClientIp.de(embrulhada));
        assertNotEquals("1.2.3.4", ClientIp.de(embrulhada));
    }
}
