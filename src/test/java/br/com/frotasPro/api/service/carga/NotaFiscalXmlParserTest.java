package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.integracao.dto.NotaFiscalXmlDto;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotaFiscalXmlParserTest {

    private final NotaFiscalXmlParser parser = new NotaFiscalXmlParser();

    private static final String NFE_BASE = """
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
              <infNFe>
                <ide><nNF> 12345 </nNF></ide>
                <dest>
                  <CNPJ>12345678000199</CNPJ>
                  <xNome> Mercadinho do Zé </xNome>
                  <enderDest>
                    <xLgr>Rua das Flores</xLgr>
                    <nro>100</nro>
                    <xCpl>Loja 2</xCpl>
                    <xBairro>Centro</xBairro>
                    <xMun>Recife</xMun>
                    <UF>PE</UF>
                    <CEP>50000000</CEP>
                    <fone>8133334444</fone>
                  </enderDest>
                  <email>contato@mercadinho.com</email>
                </dest>
                <total><ICMSTot><vNF>1500.75</vNF></ICMSTot></total>
                <transp><vol><pesoB>320.500</pesoB></vol></transp>
              </infNFe>
            </NFe>
            """;

    private MockMultipartFile arquivo(String nome, String conteudo) {
        return new MockMultipartFile("file", nome, "text/xml", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void leTodosOsCamposDeUmaNfeComNamespace() {
        NotaFiscalXmlDto dto = parser.parse(NFE_BASE);

        assertEquals("12345", dto.numeroNota());
        assertEquals("Mercadinho do Zé", dto.nomeCliente());
        assertEquals("Recife", dto.cidadeCliente());
        assertEquals(new BigDecimal("1500.75"), dto.valorTotal());
        assertEquals(new BigDecimal("320.500"), dto.pesoBruto());
        assertEquals("12345678000199", dto.documentoCliente());
        assertEquals("Rua das Flores", dto.logradouroCliente());
        assertEquals("100", dto.numeroCliente());
        assertEquals("Loja 2", dto.complementoCliente());
        assertEquals("Centro", dto.bairroCliente());
        assertEquals("PE", dto.ufCliente());
        assertEquals("50000000", dto.cepCliente());
        assertEquals("8133334444", dto.telefoneCliente());
        assertEquals("contato@mercadinho.com", dto.emailCliente());
    }

    @Test
    void leNfeEmbrulhadaEmNfeProc() {
        String xml = "<nfeProc xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"4.00\">" + NFE_BASE
                + "<protNFe><infProt><nProt>1</nProt></infProt></protNFe></nfeProc>";

        NotaFiscalXmlDto dto = parser.parse(xml);

        assertEquals("12345", dto.numeroNota());
        assertEquals("Mercadinho do Zé", dto.nomeCliente());
    }

    @Test
    void leOArquivoEnviadoPeloUsuario() {
        NotaFiscalXmlDto dto = parser.parse(arquivo("nota.XML", NFE_BASE));

        assertEquals("12345", dto.numeroNota());
    }

    @Test
    void semCnpjUsaOCpfDoDestinatario() {
        String xml = NFE_BASE.replace("<CNPJ>12345678000199</CNPJ>", "<CPF>12345678901</CPF>");

        assertEquals("12345678901", parser.parse(xml).documentoCliente());
    }

    @Test
    void comCnpjECpfPrefereOCnpj() {
        String xml = NFE_BASE.replace("<CNPJ>12345678000199</CNPJ>", "<CNPJ>12345678000199</CNPJ><CPF>12345678901</CPF>");

        assertEquals("12345678000199", parser.parse(xml).documentoCliente());
    }

    @Test
    void camposOpcionaisAusentesViramNulo() {
        String xml = NFE_BASE
                .replace("<pesoB>320.500</pesoB>", "")
                .replace("<xCpl>Loja 2</xCpl>", "")
                .replace("<email>contato@mercadinho.com</email>", "");

        NotaFiscalXmlDto dto = parser.parse(xml);

        assertNull(dto.pesoBruto());
        assertNull(dto.complementoCliente());
        assertNull(dto.emailCliente());
        assertEquals("12345", dto.numeroNota());
    }

    @Test
    void valorOuPesoNaoNumericosViramNuloSemQuebrar() {
        String xml = NFE_BASE.replace("1500.75", "abc").replace("320.500", "pesado");

        NotaFiscalXmlDto dto = parser.parse(xml);

        assertNull(dto.valorTotal());
        assertNull(dto.pesoBruto());
    }

    @Test
    void semNumeroDaNotaOuSemClienteDaErro() {
        String semNumero = NFE_BASE.replace("<nNF> 12345 </nNF>", "");
        String semCliente = NFE_BASE.replace("<xNome> Mercadinho do Zé </xNome>", "");

        assertThrows(BusinessException.class, () -> parser.parse(semNumero));
        assertThrows(BusinessException.class, () -> parser.parse(semCliente));
    }

    @Test
    void xmlMalFormadoDaErroLegivel() {
        BusinessException e = assertThrows(BusinessException.class, () -> parser.parse("<NFe><ide>"));

        assertTrue(e.getMessage().contains("Não foi possível ler"));
    }

    @Test
    void entradaVaziaOuNulaDaErro() {
        assertThrows(BusinessException.class, () -> parser.parse((String) null));
        assertThrows(BusinessException.class, () -> parser.parse("   "));
        assertThrows(BusinessException.class, () -> parser.parse((org.springframework.web.multipart.MultipartFile) null));
        assertThrows(BusinessException.class, () -> parser.parse(arquivo("nota.xml", "")));
    }

    @Test
    void arquivoQueNaoTerminaEmXmlEhRecusado() {
        BusinessException e = assertThrows(BusinessException.class, () -> parser.parse(arquivo("nota.pdf", NFE_BASE)));

        assertTrue(e.getMessage().contains("não é um XML de NFe"));
    }

    @Test
    void recusaXmlComDoctypeParaNaoFicarExpostoAXxe() {
        String xxe = """
                <?xml version="1.0"?>
                <!DOCTYPE NFe [<!ENTITY segredo SYSTEM "file:///etc/passwd">]>
                <NFe><infNFe><ide><nNF>1</nNF></ide><dest><xNome>&segredo;</xNome></dest></infNFe></NFe>
                """;

        assertThrows(BusinessException.class, () -> parser.parse(xxe));
        assertThrows(BusinessException.class, () -> parser.parse(arquivo("xxe.xml", xxe)));
    }
}
