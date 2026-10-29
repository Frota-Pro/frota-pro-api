package br.com.frotasPro.api.util;

import java.security.SecureRandom;

/**
 * Senha inicial aleatória (SecureRandom), fácil de ditar e anotar: sem caracteres que se
 * confundem (0/O, 1/l/I). 10 caracteres de um alfabeto de 55 dão cerca de 58 bits.
 */
public final class GeradorSenha {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int TAMANHO = 10;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeradorSenha() {
    }

    public static String gerar() {
        StringBuilder senha = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            senha.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return senha.toString();
    }
}
