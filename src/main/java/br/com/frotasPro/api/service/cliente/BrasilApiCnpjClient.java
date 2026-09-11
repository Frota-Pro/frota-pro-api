package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.integracao.dto.BrasilApiCnpjDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Consulta pública de CNPJ (Receita Federal) via BrasilAPI — gratuita, sem
 * chave — só pra pré-preencher o cadastro manual de Cliente. Não guarda
 * nada localmente; se a BrasilAPI estiver fora do ar, o cadastro continua
 * funcionando normalmente, só sem o preenchimento automático.
 */
@Slf4j
@Component
public class BrasilApiCnpjClient {

    private static final String BASE_URL = "https://brasilapi.com.br/api/cnpj/v1/";

    private final RestTemplate restTemplate;

    public BrasilApiCnpjClient(RestTemplateBuilder restTemplateBuilder,
                               @Value("${frotapro.consulta-cnpj.timeout-ms:5000}") long timeoutMs) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    public BrasilApiCnpjDto consultar(String cnpj) {
        try {
            BrasilApiCnpjDto resposta = restTemplate.getForObject(BASE_URL + cnpj, BrasilApiCnpjDto.class);
            if (resposta == null) {
                throw new ObjectNotFound("CNPJ não encontrado.");
            }
            return resposta;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ObjectNotFound("CNPJ não encontrado na Receita Federal.");
        } catch (HttpClientErrorException.BadRequest e) {
            // BrasilAPI responde 400 tanto pra CNPJ com dígito verificador
            // inválido quanto pra formato errado — não dá pra distinguir do
            // corpo da resposta com segurança, então cobre os dois casos.
            throw new BusinessException("CNPJ inválido ou não encontrado na Receita Federal.");
        } catch (HttpClientErrorException e) {
            log.warn("BrasilAPI retornou erro ao consultar CNPJ {}: {}", cnpj, e.getStatusCode());
            throw new BusinessException("Não foi possível consultar o CNPJ agora. Tente novamente em instantes.");
        } catch (ResourceAccessException e) {
            log.warn("Falha de rede ao consultar CNPJ {} na BrasilAPI.", cnpj, e);
            throw new BusinessException("Serviço de consulta de CNPJ indisponível no momento.");
        }
    }
}
