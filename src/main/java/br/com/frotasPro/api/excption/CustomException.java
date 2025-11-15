package br.com.frotasPro.api.excption;

import java.time.Instant;

public class CustomException {

    private Instant timestamp;
    private Integer status;
    private String error;
    private String path;

    public CustomException(Instant timestamp, Integer status, String error, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }
}
