package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.controller.response.ConfiguracaoEmpresaResponse;
import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
import br.com.frotasPro.api.service.configuracaoempresa.ConfiguracaoEmpresaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Envia e-mail com XML + PDF da nota fiscal anexados, via API do Resend
 * (resend.com). Não guarda nada — só repassa o que já veio do WinThor.
 * Remetente/assunto/corpo vêm da Configuração da Empresa (editável pelo
 * admin); os valores em application.yaml só servem de fallback caso a
 * empresa ainda não tenha configurado nada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResendEmailService {

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final String ASSUNTO_PADRAO = "Nota fiscal {numeroNota} - FrotaPRO";
    private static final String CORPO_PADRAO = "<p>Segue em anexo o XML e o DANFE (PDF) da nota fiscal {numeroNota}.</p>";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConfiguracaoEmpresaService configuracaoEmpresaService;

    @Value("${frotapro.resend.api-key:}")
    private String apiKey;

    @Value("${frotapro.resend.remetente:}")
    private String remetentePadrao;

    public void enviarNotaFiscal(String destinatario, Long numeroNota, String xml, byte[] pdf) {
        ConfiguracaoEmpresaResponse config = configuracaoEmpresaService.buscar();

        String remetente = primeiroNaoBranco(config.getEmailRemetente(), remetentePadrao);

        if (apiKey == null || apiKey.isBlank() || remetente == null || remetente.isBlank()) {
            throw new IntegracaoIndisponivelException(
                    "Envio de e-mail não configurado (falta a chave de API do Resend ou o remetente).");
        }

        String assunto = substituirPlaceholders(
                primeiroNaoBranco(config.getEmailAssunto(), ASSUNTO_PADRAO), config, numeroNota);
        String corpo = substituirPlaceholders(
                primeiroNaoBranco(config.getEmailCorpoHtml(), CORPO_PADRAO), config, numeroNota);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "from", remetente,
                "to", List.of(destinatario),
                "subject", assunto,
                "html", corpo,
                "attachments", List.of(
                        Map.of(
                                "filename", "NF_" + numeroNota + ".xml",
                                "content", Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8))
                        ),
                        Map.of(
                                "filename", "NF_" + numeroNota + ".pdf",
                                "content", Base64.getEncoder().encodeToString(pdf)
                        )
                )
        );

        try {
            restTemplate.postForEntity(RESEND_URL, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException e) {
            log.error("Falha ao enviar e-mail via Resend", e);
            throw new IntegracaoIndisponivelException(
                    "Não foi possível enviar o e-mail no momento. Tente novamente em alguns minutos.", e);
        }
    }

    private String substituirPlaceholders(String texto, ConfiguracaoEmpresaResponse config, Long numeroNota) {
        String nomeEmpresa = config.getNomeEmpresa() != null ? config.getNomeEmpresa() : "";
        return texto
                .replace("{numeroNota}", String.valueOf(numeroNota))
                .replace("{nomeEmpresa}", nomeEmpresa);
    }

    private String primeiroNaoBranco(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
