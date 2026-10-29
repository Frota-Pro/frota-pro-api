package br.com.frotasPro.api.controller;

import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.repository.ArquivoRepository;
import br.com.frotasPro.api.service.arquivo.TipoArquivoSeguro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArquivoControllerTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] PDF = "%PDF-1.7\n%conteudo".getBytes(StandardCharsets.ISO_8859_1);
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    private static final byte[] HTML = "<html><script>fetch('/roubar')</script></html>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] XML = "<?xml version=\"1.0\"?><html xmlns=\"http://www.w3.org/1999/xhtml\"><script>1</script></html>"
            .getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path pasta;

    private ArquivoRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(ArquivoRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new ArquivoController(repository)).build();
    }

    private UUID guardar(String nomeOriginal, String contentTypeDeclarado, byte[] conteudo) throws Exception {
        Path arquivo = Files.write(pasta.resolve(UUID.randomUUID() + ".bin"), conteudo);
        Arquivo a = new Arquivo();
        a.setId(UUID.randomUUID());
        a.setNomeOriginal(nomeOriginal);
        a.setCaminho(arquivo.toString());
        a.setContentType(contentTypeDeclarado);
        when(repository.findById(a.getId())).thenReturn(Optional.of(a));
        return a.getId();
    }

    @Test
    void pdfEImagensSaoExibidosNoNavegadorComOTipoCerto() throws Exception {
        mvc.perform(get("/arquivos/" + guardar("cnh.pdf", "application/pdf", PDF) + "/preview"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("inline")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mvc.perform(get("/arquivos/" + guardar("foto.png", "image/png", PNG) + "/preview"))
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("inline")));
        mvc.perform(get("/arquivos/" + guardar("foto.jpg", null, JPEG) + "/preview"))
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void htmlEnviadoComoImagemNaoRodaNoNavegador() throws Exception {
        // o cliente declarou image/png, mas o conteúdo é HTML com script
        mvc.perform(get("/arquivos/" + guardar("foto.png", "image/png", HTML) + "/preview"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void svgHtmlEXmlDeclaradosComoTextoSaemSempreComoDownload() throws Exception {
        for (Object[] caso : new Object[][]{
                {"logo.svg", "image/svg+xml", SVG},
                {"pagina.html", "text/html", HTML},
                {"nota.xml", "application/xml", XML},
                {"xhtml.xml", "application/xhtml+xml", XML}}) {
            mvc.perform(get("/arquivos/" + guardar((String) caso[0], (String) caso[1], (byte[]) caso[2]) + "/preview"))
                    .andExpect(header().string("Content-Type", "application/octet-stream"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")));
        }
    }

    @Test
    void downloadSempreEAnexoMesmoParaPdf() throws Exception {
        mvc.perform(get("/arquivos/" + guardar("cnh.pdf", "application/pdf", PDF) + "/download"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                .andExpect(header().string("Content-Type", "application/pdf"));
        mvc.perform(get("/arquivos/" + guardar("pagina.html", "text/html", HTML) + "/download"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment")))
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }

    @Test
    void nomeDeArquivoComAspasEAcentoNaoQuebraNemInjetaNoCabecalho() throws Exception {
        String disposicao = mvc.perform(get("/arquivos/" + guardar("rel\"; filename=\"evil.html", "application/pdf", PDF) + "/download"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Content-Disposition");
        assertFalse(disposicao.contains("filename=\"rel\";"), disposicao);

        String acentuado = mvc.perform(get("/arquivos/" + guardar("Habilitação.pdf", "application/pdf", PDF) + "/download"))
                .andReturn().getResponse().getHeader("Content-Disposition");
        assertTrue(acentuado.contains("filename*=UTF-8''Habilita"), acentuado);
    }

    // ---- deteccao e gravacao

    @Test
    void deteccaoPorConteudoSoReconheceOsTiposSeguros() {
        assertEquals("application/pdf", TipoArquivoSeguro.detectar(PDF).get().toString());
        assertEquals("image/png", TipoArquivoSeguro.detectar(PNG).get().toString());
        assertEquals("image/jpeg", TipoArquivoSeguro.detectar(JPEG).get().toString());
        assertEquals("image/gif", TipoArquivoSeguro.detectar("GIF89a......".getBytes()).get().toString());
        assertEquals("image/webp", TipoArquivoSeguro.detectar("RIFF\u0001\u0002\u0003\u0004WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1)).get().toString());
        assertTrue(TipoArquivoSeguro.detectar(HTML).isEmpty());
        assertTrue(TipoArquivoSeguro.detectar(SVG).isEmpty());
        assertTrue(TipoArquivoSeguro.detectar(XML).isEmpty());
        assertTrue(TipoArquivoSeguro.detectar(new byte[]{1, 2}).isEmpty());
        assertTrue(TipoArquivoSeguro.detectar((byte[]) null).isEmpty());
    }
}
