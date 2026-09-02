package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.integracao.dto.NotaFiscalXmlDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Lê o XML de uma NFe (modelo 55) já emitida em outro sistema — não emite,
 * não assina, não fala com a SEFAZ, só extrai os dados que hoje vêm prontos
 * do WinThor (cliente, cidade, número da nota, peso, valor) pra permitir
 * cadastrar essas informações numa carga criada na mão, sem a integração.
 * <p>
 * Usa local-name() no XPath em vez de prefixo de namespace, porque o XML
 * pode vir como {@code <NFe>} solto ou embrulhado em {@code <nfeProc>}
 * (com o protocolo de autorização anexado), e ambos declaram o mesmo
 * namespace default (http://www.portalfiscal.inf.br/nfe) de formas
 * ligeiramente diferentes dependendo de quem gerou o arquivo.
 */
@Component
public class NotaFiscalXmlParser {

    public NotaFiscalXmlDto parse(MultipartFile arquivoXml) {
        if (arquivoXml == null || arquivoXml.isEmpty()) {
            throw new BusinessException("Arquivo XML não informado ou vazio.");
        }

        String nomeArquivo = arquivoXml.getOriginalFilename();
        if (nomeArquivo != null && !nomeArquivo.toLowerCase().endsWith(".xml")) {
            throw new BusinessException("Arquivo \"" + nomeArquivo + "\" não é um XML de NFe.");
        }

        Document documento;
        try (InputStream in = arquivoXml.getInputStream()) {
            documento = criarDocumentBuilder().parse(in);
        } catch (SAXException e) {
            throw new BusinessException(
                    "Não foi possível ler \"" + nomeArquivo + "\" — verifique se é um XML de NFe válido.", e);
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler o arquivo \"" + nomeArquivo + "\".", e);
        }

        XPath xpath = XPathFactory.newInstance().newXPath();

        String numeroNota = textoOuNulo(documento, xpath, "//*[local-name()='ide']/*[local-name()='nNF']");
        String nomeCliente = textoOuNulo(documento, xpath,
                "//*[local-name()='dest']/*[local-name()='xNome']");
        String cidadeCliente = textoOuNulo(documento, xpath,
                "//*[local-name()='dest']/*[local-name()='enderDest']/*[local-name()='xMun']");
        String valorTotalStr = textoOuNulo(documento, xpath,
                "//*[local-name()='total']/*[local-name()='ICMSTot']/*[local-name()='vNF']");
        String pesoBrutoStr = textoOuNulo(documento, xpath,
                "//*[local-name()='transp']/*[local-name()='vol']/*[local-name()='pesoB']");

        if (numeroNota == null || numeroNota.isBlank() || nomeCliente == null || nomeCliente.isBlank()) {
            throw new BusinessException(
                    "Não foi possível encontrar o número da nota e/ou o cliente em \"" + nomeArquivo
                            + "\" — verifique se é um XML de NFe (modelo 55) válido.");
        }

        return NotaFiscalXmlDto.builder()
                .numeroNota(numeroNota.trim())
                .nomeCliente(nomeCliente.trim())
                .cidadeCliente(cidadeCliente != null ? cidadeCliente.trim() : null)
                .valorTotal(paraBigDecimal(valorTotalStr))
                .pesoBruto(paraBigDecimal(pesoBrutoStr))
                .build();
    }

    private String textoOuNulo(Document documento, XPath xpath, String expressao) {
        try {
            String texto = (String) xpath.evaluate(expressao, documento, XPathConstants.STRING);
            return texto == null || texto.isBlank() ? null : texto;
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal paraBigDecimal(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * XML de upload é entrada não confiável — desliga DTD/entidades externas
     * pra não ficar exposto a XXE (o parser só precisa ler texto, nunca
     * precisou resolver entidade nenhuma de fora do próprio arquivo).
     */
    private DocumentBuilder criarDocumentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Erro ao configurar o parser de XML.", e);
        }
    }
}
