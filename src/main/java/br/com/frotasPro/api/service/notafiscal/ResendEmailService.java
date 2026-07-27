package br.com.frotasPro.api.service.notafiscal;

import br.com.frotasPro.api.excption.IntegracaoIndisponivelException;
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
 */
@Slf4j
@Service
public class ResendEmailService {

    private static final String RESEND_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${frotapro.resend.api-key:}")
    private String apiKey;

    @Value("${frotapro.resend.remetente:}")
    private String remetente;

    public void enviarNotaFiscal(String destinatario, Long numeroNota, String xml, byte[] pdf) {
        if (apiKey == null || apiKey.isBlank() || remetente == null || remetente.isBlank()) {
            throw new IntegracaoIndisponivelException(
                    "Envio de e-mail não configurado (falta a chave de API do Resend ou o remetente).");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "from", remetente,
                "to", List.of(destinatario),
                "subject", "Nota fiscal " + numeroNota + " - FrotaPRO",
                "html", "<p>Segue em anexo o XML e o DANFE (PDF) da nota fiscal " + numeroNota + ".</p>",
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
}
