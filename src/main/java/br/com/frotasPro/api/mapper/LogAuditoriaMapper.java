package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.LogAuditoriaResponse;
import br.com.frotasPro.api.domain.LogAuditoria;

public class LogAuditoriaMapper {

    public static LogAuditoriaResponse toResponse(LogAuditoria log) {
        if (log == null) {
            return null;
        }

        return LogAuditoriaResponse.builder()
                .id(log.getId())
                .dataHora(log.getDataHora())
                .usuarioLogin(log.getUsuarioLogin())
                .usuarioNome(log.getUsuarioNome())
                .acao(log.getAcao() != null ? log.getAcao().name() : null)
                .acaoLabel(log.getAcao() != null ? log.getAcao().getLabel() : null)
                .entidade(log.getEntidade())
                .descricao(log.getDescricao())
                .metodoHttp(log.getMetodoHttp())
                .endpoint(log.getEndpoint())
                .statusHttp(log.getStatusHttp())
                .ip(log.getIp())
                .build();
    }
}
