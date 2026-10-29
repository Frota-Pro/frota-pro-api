package br.com.frotasPro.api.service.arquivo;

import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * O que cada tipo de envio aceita. Antes qualquer arquivo era gravado, com a extensão e o tipo que o
 * cliente escolhesse (.html, .svg, .exe). Agora o conteúdo precisa ser de um tipo da categoria,
 * conferido pelos primeiros bytes, e a extensão gravada no disco sai do tipo detectado.
 */
public enum CategoriaArquivo {

    /** Anexos e documentos (CNH, CRLV, notas, multas, fotos de parada): PDF e imagens. */
    DOCUMENTO(20L * 1024 * 1024, "um PDF ou uma imagem (JPG, PNG, GIF ou WEBP)"),

    IMAGEM(3L * 1024 * 1024, "uma imagem (JPG, PNG, GIF ou WEBP)"),

    /** XML de nota fiscal (a leitura em si tem proteção contra XXE no NotaFiscalXmlParser). */
    XML(2L * 1024 * 1024, "um arquivo XML"),

    APK(200L * 1024 * 1024, "um arquivo .apk");

    public record Tipo(MediaType mediaType, String extensao) {
    }

    private final long limiteBytes;
    private final String descricao;

    CategoriaArquivo(long limiteBytes, String descricao) {
        this.limiteBytes = limiteBytes;
        this.descricao = descricao;
    }

    public long limiteBytes() {
        return limiteBytes;
    }

    public String descricao() {
        return descricao;
    }

    /** O tipo do conteúdo, se for um dos aceitos nesta categoria; vazio se não for. */
    public Optional<Tipo> reconhecer(byte[] conteudo) {
        return switch (this) {
            case DOCUMENTO -> TipoArquivoSeguro.detectar(conteudo).map(CategoriaArquivo::tipoDe);
            case IMAGEM -> TipoArquivoSeguro.detectar(conteudo)
                    .filter(tipo -> !MediaType.APPLICATION_PDF.equals(tipo))
                    .map(CategoriaArquivo::tipoDe);
            case XML -> ehXmlSemDtd(conteudo) ? Optional.of(new Tipo(MediaType.APPLICATION_XML, ".xml")) : Optional.empty();
            case APK -> ehZip(conteudo)
                    ? Optional.of(new Tipo(MediaType.parseMediaType("application/vnd.android.package-archive"), ".apk"))
                    : Optional.empty();
        };
    }

    private static Tipo tipoDe(MediaType tipo) {
        String extensao = switch (tipo.getSubtype()) {
            case "pdf" -> ".pdf";
            case "png" -> ".png";
            case "jpeg" -> ".jpg";
            case "gif" -> ".gif";
            case "webp" -> ".webp";
            default -> ".bin";
        };
        return new Tipo(tipo, extensao);
    }

    private static boolean ehZip(byte[] c) {
        return c != null && c.length >= 4 && c[0] == 0x50 && c[1] == 0x4B && c[2] == 0x03 && c[3] == 0x04;
    }

    /** Começa com "<" (depois de BOM/espaços) e não declara DTD nem entidades (nota fiscal nunca precisa). */
    private static boolean ehXmlSemDtd(byte[] c) {
        if (c == null || c.length < 5) {
            return false;
        }
        int i = 0;
        if (c.length >= 3 && (c[0] & 0xFF) == 0xEF && (c[1] & 0xFF) == 0xBB && (c[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        while (i < c.length && (c[i] == ' ' || c[i] == '\n' || c[i] == '\r' || c[i] == '\t')) {
            i++;
        }
        if (i >= c.length || c[i] != '<') {
            return false;
        }
        String inicio = new String(c, 0, Math.min(c.length, 4096), StandardCharsets.ISO_8859_1).toUpperCase();
        return !inicio.contains("<!DOCTYPE") && !inicio.contains("<!ENTITY");
    }
}
