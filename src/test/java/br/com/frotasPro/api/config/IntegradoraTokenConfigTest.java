package br.com.frotasPro.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IntegradoraTokenConfigTest {

    private RestTemplate comTokenConfigurado(String token) {
        RestTemplate restTemplate = new RestTemplate();
        new IntegradoraTokenConfig().integradoraTokenCustomizer("http://integradora:8081/", token).customize(restTemplate);
        return restTemplate;
    }

    @Test
    void mandaOTokenSoParaAIntegradora() {
        RestTemplate restTemplate = comTokenConfigurado(" segredo ");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();

        servidor.expect(requestTo("http://integradora:8081/winthor/cargas/1/notas"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Integradora-Token", "segredo"))
                .andRespond(withSuccess("ok", org.springframework.http.MediaType.TEXT_PLAIN));
        servidor.expect(requestTo("https://brasilapi.com.br/api/cnpj/v1/1"))
                .andExpect(request -> assertFalse(request.getHeaders().containsKey("X-Integradora-Token")))
                .andRespond(withSuccess("ok", org.springframework.http.MediaType.TEXT_PLAIN));

        restTemplate.getForObject("http://integradora:8081/winthor/cargas/1/notas", String.class);
        restTemplate.getForObject("https://brasilapi.com.br/api/cnpj/v1/1", String.class);

        servidor.verify();
    }

    @Test
    void semTokenConfiguradoNaoMandaCabecalhoNenhum() {
        RestTemplate restTemplate = comTokenConfigurado("");
        assertTrue(restTemplate.getInterceptors().isEmpty());
    }

    @Test
    void reconhecePortaPadraoEHostSemDiferenciarCaixa() {
        assertTrue(IntegradoraTokenConfig.mesmoServidor(
                java.net.URI.create("https://Integradora.local"), java.net.URI.create("https://integradora.local/x")));
        assertFalse(IntegradoraTokenConfig.mesmoServidor(
                java.net.URI.create("http://integradora:8081"), java.net.URI.create("http://integradora:9999/x")));
        assertEquals(false, IntegradoraTokenConfig.mesmoServidor(
                java.net.URI.create("http://integradora:8081"), java.net.URI.create("http://outro:8081/x")));
    }
}
