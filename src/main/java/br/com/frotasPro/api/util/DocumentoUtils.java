package br.com.frotasPro.api.util;

public final class DocumentoUtils {

    private DocumentoUtils() {
    }

    /** Só dígitos — CNPJ/CPF às vezes vem formatado (com . / -), às vezes não. */
    public static String normalizar(String documento) {
        if (documento == null) {
            return null;
        }
        String digitos = documento.replaceAll("\\D", "");
        return digitos.isBlank() ? null : digitos;
    }
}
