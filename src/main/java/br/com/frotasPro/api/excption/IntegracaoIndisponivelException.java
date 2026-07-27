package br.com.frotasPro.api.excption;

/**
 * A integradora (ou o WinThor por trás dela) está indisponível/instável
 * no momento. Erro temporário — o chamador deve tentar novamente depois.
 */
public class IntegracaoIndisponivelException extends RuntimeException {
    public IntegracaoIndisponivelException(String message) {
        super(message);
    }

    public IntegracaoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
