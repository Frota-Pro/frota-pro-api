package br.com.frotasPro.api.mapper;

import br.com.frotasPro.api.controller.response.NotificacaoResponse;
import br.com.frotasPro.api.domain.NotificacaoUsuario;

public class NotificacaoMapper {

    private NotificacaoMapper() {
    }

    public static NotificacaoResponse toResponse(NotificacaoUsuario entity) {
        if (entity == null || entity.getNotificacao() == null) {
            return null;
        }

        var n = entity.getNotificacao();

        return NotificacaoResponse.builder()
                .id(n.getId())
                .evento(n.getEvento())
                .tipo(n.getTipo())
                .titulo(n.getTitulo())
                .mensagem(n.getMensagem())
                .referenciaTipo(n.getReferenciaTipo())
                .referenciaId(n.getReferenciaId())
                .referenciaCodigo(n.getReferenciaCodigo())
                .criadoEm(n.getCriadoEm())
                .lidaEm(entity.getLidaEm())
                .lida(entity.getLidaEm() != null)
                .build();
    }
}
