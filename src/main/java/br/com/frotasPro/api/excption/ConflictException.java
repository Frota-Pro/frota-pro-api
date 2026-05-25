package br.com.frotasPro.api.excption;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
