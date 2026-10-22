package br.com.frotasPro.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração do BI (Metabase) mostrado no app do gerente. O Metabase continua privado:
 * a API só assina links curtos de "incorporação" (embedding) pros dashboards da lista abaixo.
 * Sem o segredo configurado o BI fica desligado (503), sem afetar o resto da API.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "frotapro.bi")
public class BiProperties {

    /** Endereço do Metabase como o celular enxerga (ex.: https://bi.empresa.com.br). */
    private String metabaseSiteUrl = "";

    /** Segredo de incorporação do Metabase (Admin > Embedding). Nunca vai pro app nem pro log. */
    private String embeddingSecret = "";

    /** Quanto tempo o link assinado vale. Curto de propósito: o app pede um novo a cada abertura e no botão de atualizar. */
    private int tokenTtlMinutos = 30;

    private List<Dashboard> dashboards = new ArrayList<>();

    public boolean configurado() {
        return metabaseSiteUrl != null && !metabaseSiteUrl.isBlank()
                && embeddingSecret != null && !embeddingSecret.isBlank();
    }

    @Getter
    @Setter
    public static class Dashboard {
        /** ID do dashboard no Metabase (o número na URL /dashboard/{id}). */
        private int id;
        private String titulo;
        private String descricao;
        /** true = só ROLE_ADMIN vê (ex.: saúde da integração, uso técnico). */
        private boolean somenteAdmin;
    }
}
