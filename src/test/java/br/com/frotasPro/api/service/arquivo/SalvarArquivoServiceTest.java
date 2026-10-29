package br.com.frotasPro.api.service.arquivo;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.repository.ArquivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalvarArquivoServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] PDF = "%PDF-1.7\n%conteudo".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] HTML = "<html><script>fetch('/roubar')</script></html>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXE = {0x4D, 0x5A, (byte) 0x90, 0, 3, 0, 0, 0};
    private static final byte[] NFE = "<?xml version=\"1.0\"?><nfeProc><NFe/></nfeProc>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] XML_COM_DTD = ("<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><x>&e;</x>")
            .getBytes(StandardCharsets.UTF_8);
    private static final byte[] ZIP = {0x50, 0x4B, 0x03, 0x04, 0, 0, 0, 0};

    @TempDir
    Path pasta;

    private ArquivoRepository repo;
    private SalvarArquivoService servico;

    @BeforeEach
    void setUp() {
        repo = mock(ArquivoRepository.class);
        when(repo.findByHash(any())).thenReturn(Optional.empty());
        when(repo.save(any(Arquivo.class))).thenAnswer(i -> i.getArgument(0));
        servico = new SalvarArquivoService(repo);
        ReflectionTestUtils.setField(servico, "uploadDir", pasta.toString());
    }

    private MockMultipartFile arquivo(String nome, String tipoDeclarado, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nome, tipoDeclarado, conteudo);
    }

    private void assertRecusado(Runnable envio, String trecho) {
        org.mockito.Mockito.clearInvocations(repo);
        BusinessException erro = assertThrows(BusinessException.class, envio::run);
        assertTrue(erro.getMessage().contains(trecho), erro.getMessage());
        verify(repo, never()).save(any());
    }

    private long arquivosNoDisco() throws IOException {
        try (Stream<Path> s = Files.walk(pasta)) {
            return s.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void documentoAceitaPdfEImagemEGravaComAExtensaoDoConteudo() throws Exception {
        Arquivo pdf = servico.salvar(arquivo("cnh.html", "text/html", PDF), "MOTORISTA_X", "CNH");
        Arquivo png = servico.salvar(arquivo("foto.exe", "application/octet-stream", PNG), "MOTORISTA_X", "CNH");

        assertEquals("application/pdf", pdf.getContentType());
        assertTrue(pdf.getCaminho().endsWith(".pdf"), pdf.getCaminho());
        assertEquals("image/png", png.getContentType());
        assertTrue(png.getCaminho().endsWith(".png"), png.getCaminho());
        assertEquals("cnh.html", pdf.getNomeOriginal());
    }

    @Test
    void documentoRecusaHtmlSvgExecutavelEZipSemGravarNada() throws Exception {
        for (Object[] caso : new Object[][]{
                {"foto.png", "image/png", HTML},
                {"logo.svg", "image/svg+xml", SVG},
                {"setup.pdf", "application/pdf", EXE},
                {"pacote.pdf", "application/pdf", ZIP},
                {"nota.xml", "text/xml", NFE}}) {
            assertRecusado(() -> servico.salvar(arquivo((String) caso[0], (String) caso[1], (byte[]) caso[2]), "P", "T"),
                    "Tipo de arquivo não permitido");
        }
        assertEquals(0, arquivosNoDisco());
    }

    @Test
    void mensagemDeRecusaDizOQueEPermitido() {
        assertRecusado(() -> servico.salvar(arquivo("x.html", "text/html", HTML), "P", "T"), "PDF ou uma imagem");
    }

    @Test
    void logoAceitaSoImagem() {
        assertEquals("image/png", servico.salvar(arquivo("logo.png", "image/png", PNG), "EMPRESA", "LOGO", CategoriaArquivo.IMAGEM).getContentType());
        assertRecusado(() -> servico.salvar(arquivo("logo.pdf", "application/pdf", PDF), "EMPRESA", "LOGO", CategoriaArquivo.IMAGEM),
                "uma imagem");
    }

    @Test
    void notaFiscalAceitaXmlMasNaoComDtdOuEntidades() {
        Arquivo xml = servico.salvar(arquivo("nfe.xml", "text/xml", NFE), "CARGA_1", "NOTA_FISCAL_XML", CategoriaArquivo.XML);
        assertEquals("application/xml", xml.getContentType());
        assertTrue(xml.getCaminho().endsWith(".xml"));

        assertRecusado(() -> servico.salvar(arquivo("xxe.xml", "text/xml", XML_COM_DTD), "CARGA_1", "NOTA_FISCAL_XML", CategoriaArquivo.XML),
                "XML");
        assertRecusado(() -> servico.salvar(arquivo("nfe.xml", "text/xml", PNG), "CARGA_1", "NOTA_FISCAL_XML", CategoriaArquivo.XML),
                "XML");
    }

    @Test
    void apkPrecisaTerCaraDeZip() {
        Arquivo apk = servico.salvar(arquivo("app.apk", "application/octet-stream", ZIP), "app", "1.8.0", CategoriaArquivo.APK);
        assertEquals("application/vnd.android.package-archive", apk.getContentType());
        assertTrue(apk.getCaminho().endsWith(".apk"));

        assertRecusado(() -> servico.salvar(arquivo("app.apk", "application/octet-stream", HTML), "app", "1.8.1", CategoriaArquivo.APK),
                ".apk");
    }

    @Test
    void arquivoMaiorQueOLimiteDaCategoriaERecusadoAntesDeLer() {
        byte[] grande = new byte[(int) CategoriaArquivo.IMAGEM.limiteBytes() + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);

        assertRecusado(() -> servico.salvar(arquivo("logo.png", "image/png", grande), "EMPRESA", "LOGO", CategoriaArquivo.IMAGEM),
                "muito grande");
    }

    @Test
    void arquivoVazioOuNuloERecusado() {
        assertRecusado(() -> servico.salvar(arquivo("a.pdf", "application/pdf", new byte[0]), "P", "T"), "vazio");
        assertRecusado(() -> servico.salvar(null, "P", "T"), "vazio");
    }

    @Test
    void nomeParaExibirNaoTemDiretorioNemCaracterDeControleENaoVazaParaOCaminhoNoDisco() {
        Arquivo a = servico.salvar(arquivo("..\\..\\etc/passwd\n.pdf", "application/pdf", PDF), "P", "T");

        assertFalse(a.getNomeOriginal().contains("/"));
        assertFalse(a.getNomeOriginal().contains("\\"));
        assertFalse(a.getNomeOriginal().contains("\n"));
        assertEquals("passwd.pdf", a.getNomeOriginal());
        assertTrue(Path.of(a.getCaminho()).startsWith(pasta));
        assertFalse(a.getCaminho().contains("passwd"));
    }

    @Test
    void arquivoRepetidoReaproveitaORegistroSemRevalidarOEspaco() {
        Arquivo existente = new Arquivo();
        existente.setNomeOriginal("ja-existe.pdf");
        when(repo.findByHash(any())).thenReturn(Optional.of(existente));

        assertEquals(existente, servico.salvar(arquivo("copia.pdf", "application/pdf", PDF), "P", "T"));
        verify(repo, never()).save(any());
    }
}
