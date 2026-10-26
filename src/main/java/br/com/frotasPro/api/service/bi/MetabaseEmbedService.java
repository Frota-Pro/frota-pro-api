package br.com.frotasPro.api.service.bi;

import br.com.frotasPro.api.config.BiProperties;
import br.com.frotasPro.api.controller.response.BiDashboardResponse;
import br.com.frotasPro.api.controller.response.BiEmbedUrlResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Gera os links dos dashboards do Metabase pro app do gerente. O link é a "incorporação
 * assinada" do Metabase: um JWT HS256 com o dashboard e a validade, que o Metabase confere
 * com o segredo. Quem não tem o link não vê nada, e o link vence em poucos minutos.
 */
@Service
@RequiredArgsConstructor
public class MetabaseEmbedService {

    private final BiProperties props;

    public List<BiDashboardResponse> listar(boolean admin) {
        return props.getDashboards().stream()
                .filter(d -> admin || !d.isSomenteAdmin())
                .map(d -> new BiDashboardResponse(d.getId(), d.getTitulo(), d.getDescricao(), d.getCategoria(), d.getIcone()))
                .toList();
    }

    /** Dados para a API buscar os cartões de um dashboard no Metabase (token curto, nunca vai ao app). */
    public record AcessoDados(String titulo, String token) {
    }

    public AcessoDados acessoParaDados(int dashboardId, boolean admin) {
        BiProperties.Dashboard dashboard = validar(dashboardId, admin);
        Instant expira = Instant.now().plusSeconds(300);
        return new AcessoDados(dashboard.getTitulo(), assinar(dashboardId, expira));
    }

    private BiProperties.Dashboard validar(int dashboardId, boolean admin) {
        if (!props.configurado()) {
            throw new IntegracaoIndisponivelException("O BI ainda não foi configurado neste ambiente.");
        }

        BiProperties.Dashboard dashboard = props.getDashboards().stream()
                .filter(d -> d.getId() == dashboardId)
                .findFirst()
                .orElseThrow(() -> new ObjectNotFound("Dashboard não encontrado: " + dashboardId));

        if (dashboard.isSomenteAdmin() && !admin) {
            throw new AccessDeniedException("Este dashboard é restrito ao administrador.");
        }
        return dashboard;
    }

    public BiEmbedUrlResponse gerarUrl(int dashboardId, boolean admin) {
        validar(dashboardId, admin);

        Instant expira = Instant.now().plusSeconds(60L * Math.max(1, props.getTokenTtlMinutos()));
        String token = assinar(dashboardId, expira);
        String base = props.getMetabaseSiteUrl().trim().replaceAll("/+$", "");

        // bordered/titled: o app já mostra o título do dashboard
        String url = base + "/embed/dashboard/" + token + "#bordered=false&titled=false";
        return new BiEmbedUrlResponse(dashboardId, url, expira.atOffset(ZoneOffset.UTC));
    }

    private String assinar(int dashboardId, Instant expira) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .claim("resource", Map.of("dashboard", dashboardId))
                    .claim("params", Map.of())
                    .expirationTime(Date.from(expira))
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(props.getEmbeddingSecret().getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException e) {
            // segredo curto demais para HS256 (< 32 bytes) ou erro da lib: não vaza o segredo na mensagem
            throw new IntegracaoIndisponivelException("Não foi possível assinar o link do BI. Confira o segredo de incorporação.");
        }
    }
}
