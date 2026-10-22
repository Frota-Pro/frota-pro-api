package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.config.BiProperties;
import br.com.frotasPro.api.controller.response.BiDashboardResponse;
import br.com.frotasPro.api.controller.response.BiEmbedUrlResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetabaseEmbedServiceTest {

    private static final String SEGREDO = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private BiProperties props;
    private MetabaseEmbedService service;

    @BeforeEach
    void setUp() {
        props = new BiProperties();
        props.setMetabaseSiteUrl("https://bi.exemplo.com.br/");
        props.setEmbeddingSecret(SEGREDO);
        props.setTokenTtlMinutos(10);
        props.setDashboards(List.of(
                dashboard(2, "Operação", false),
                dashboard(7, "Integração", true)));
        service = new MetabaseEmbedService(props);
    }

    private static BiProperties.Dashboard dashboard(int id, String titulo, boolean somenteAdmin) {
        BiProperties.Dashboard d = new BiProperties.Dashboard();
        d.setId(id);
        d.setTitulo(titulo);
        d.setDescricao("desc " + titulo);
        d.setSomenteAdmin(somenteAdmin);
        return d;
    }

    private static String tokenDe(String url) {
        String semBase = url.substring(url.indexOf("/embed/dashboard/") + "/embed/dashboard/".length());
        return semBase.substring(0, semBase.indexOf('#'));
    }

    @Test
    void gerenteVeSoOsDashboardsLiberados() {
        List<BiDashboardResponse> lista = service.listar(false);

        assertEquals(1, lista.size());
        assertEquals(2, lista.get(0).id());
        assertEquals("Operação", lista.get(0).titulo());
    }

    @Test
    void adminVeTodos() {
        assertEquals(List.of(2, 7), service.listar(true).stream().map(BiDashboardResponse::id).toList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void linkTemODashboardAssinadoEValidade() throws Exception {
        BiEmbedUrlResponse resp = service.gerarUrl(2, false);

        assertTrue(resp.url().startsWith("https://bi.exemplo.com.br/embed/dashboard/"), resp.url());
        assertTrue(resp.url().endsWith("#bordered=false&titled=false"));
        assertEquals(2, resp.dashboardId());

        SignedJWT jwt = SignedJWT.parse(tokenDe(resp.url()));
        assertEquals(JWSAlgorithm.HS256, jwt.getHeader().getAlgorithm());
        assertTrue(jwt.verify(new MACVerifier(SEGREDO.getBytes(StandardCharsets.UTF_8))));

        Map<String, Object> resource = (Map<String, Object>) jwt.getJWTClaimsSet().getClaim("resource");
        assertEquals(2L, ((Number) resource.get("dashboard")).longValue());
        assertEquals(Map.of(), jwt.getJWTClaimsSet().getClaim("params"));

        Date exp = jwt.getJWTClaimsSet().getExpirationTime();
        long segundos = (exp.getTime() - System.currentTimeMillis()) / 1000;
        assertTrue(segundos > 9 * 60 && segundos <= 10 * 60, "vence em ~10 min, mas venceu em " + segundos + "s");
        assertEquals(exp.toInstant().getEpochSecond(), resp.expiraEm().toInstant().getEpochSecond());
    }

    @Test
    void segredoErradoNaoValidaAAssinatura() throws Exception {
        SignedJWT jwt = SignedJWT.parse(tokenDe(service.gerarUrl(2, false).url()));

        assertFalse(jwt.verify(new MACVerifier("outro-segredo-com-pelo-menos-32-bytes-de-tamanho".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void cadaLinkEhNovoEVenceNoPrazoConfigurado() throws Exception {
        props.setTokenTtlMinutos(1);

        SignedJWT jwt = SignedJWT.parse(tokenDe(service.gerarUrl(2, false).url()));

        Instant exp = jwt.getJWTClaimsSet().getExpirationTime().toInstant();
        assertTrue(exp.isBefore(Instant.now().plusSeconds(61)));
        assertTrue(exp.isAfter(Instant.now()));
    }

    @Test
    void urlBaseComBarrasNoFimNaoDuplicaBarras() {
        props.setMetabaseSiteUrl("http://100.1.2.3:3000///");

        assertTrue(service.gerarUrl(2, false).url().startsWith("http://100.1.2.3:3000/embed/dashboard/"));
    }

    @Test
    void dashboardForaDaListaNaoExiste() {
        assertThrows(ObjectNotFound.class, () -> service.gerarUrl(999, true));
    }

    @Test
    void dashboardRestritoNegaOGerenteEDaAoAdmin() {
        assertThrows(AccessDeniedException.class, () -> service.gerarUrl(7, false));
        assertEquals(7, service.gerarUrl(7, true).dashboardId());
    }

    @Test
    void semConfiguracaoOBiFicaDesligado() {
        props.setEmbeddingSecret("");
        assertThrows(IntegracaoIndisponivelException.class, () -> service.gerarUrl(2, true));

        props.setEmbeddingSecret(SEGREDO);
        props.setMetabaseSiteUrl(" ");
        assertThrows(IntegracaoIndisponivelException.class, () -> service.gerarUrl(2, true));
    }

    @Test
    void segredoCurtoDemaisDaErroSemVazarOSegredo() {
        props.setEmbeddingSecret("curto");

        IntegracaoIndisponivelException ex =
                assertThrows(IntegracaoIndisponivelException.class, () -> service.gerarUrl(2, true));

        assertFalse(ex.getMessage().contains("curto"));
    }

    @Test
    void tokenNaoMostraOSegredoNemOsDadosDoBanco() throws ParseException {
        String token = tokenDe(service.gerarUrl(2, false).url());

        assertFalse(token.contains(SEGREDO));
        assertEquals(3, token.split("\\.").length);
    }
}
