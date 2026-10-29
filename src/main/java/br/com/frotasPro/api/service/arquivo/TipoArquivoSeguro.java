package br.com.frotasPro.api.service.arquivo;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Descobre o tipo de um arquivo pelo CONTEÚDO (os primeiros bytes), nunca pelo que o cliente
 * disse. Só os tipos que o navegador mostra sem risco de executar script: PDF e imagens
 * raster. HTML, SVG, XML e o resto não passam e são sempre entregues como download: um arquivo
 * HTML enviado por um motorista e aberto no painel do administrador rodaria script na origem do
 * sistema (acesso ao token de login).
 */
public final class TipoArquivoSeguro {

    private static final int BYTES_LIDOS = 16;

    private TipoArquivoSeguro() {
    }

    public static Optional<MediaType> detectar(Path arquivo) {
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            return detectar(entrada.readNBytes(BYTES_LIDOS));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<MediaType> detectar(byte[] inicio) {
        if (inicio == null || inicio.length < 4) {
            return Optional.empty();
        }
        if (comeca(inicio, 0x25, 0x50, 0x44, 0x46, 0x2D)) {                        // %PDF-
            return Optional.of(MediaType.APPLICATION_PDF);
        }
        if (comeca(inicio, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {      // PNG
            return Optional.of(MediaType.IMAGE_PNG);
        }
        if (comeca(inicio, 0xFF, 0xD8, 0xFF)) {                                     // JPEG
            return Optional.of(MediaType.IMAGE_JPEG);
        }
        if (comeca(inicio, 0x47, 0x49, 0x46, 0x38) && inicio.length > 4
                && (inicio[4] == 0x37 || inicio[4] == 0x39)) {                      // GIF87a / GIF89a
            return Optional.of(MediaType.IMAGE_GIF);
        }
        if (inicio.length >= 12 && comeca(inicio, 0x52, 0x49, 0x46, 0x46)           // RIFF....WEBP
                && Arrays.equals(Arrays.copyOfRange(inicio, 8, 12), new byte[]{0x57, 0x45, 0x42, 0x50})) {
            return Optional.of(MediaType.parseMediaType("image/webp"));
        }
        return Optional.empty();
    }

    /** Tipo que pode ser gravado/servido: o detectado, ou application/octet-stream. */
    public static MediaType ouBinario(Optional<MediaType> detectado) {
        return detectado.orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private static boolean comeca(byte[] bytes, int... esperados) {
        if (bytes.length < esperados.length) {
            return false;
        }
        for (int i = 0; i < esperados.length; i++) {
            if ((bytes[i] & 0xFF) != esperados[i]) {
                return false;
            }
        }
        return true;
    }
}
