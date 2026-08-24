package br.com.frotasPro.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LogAuditoriaResponse {
    private UUID id;
    private LocalDateTime dataHora;
    private String usuarioLogin;
    private String usuarioNome;
    private String acao;
    private String acaoLabel;
    private String entidade;
    private String descricao;
    private String metodoHttp;
    private String endpoint;
    private Integer statusHttp;
    private String ip;
    private Map<String, Object> dadosAntes;
    private Map<String, Object> dadosDepois;
}
