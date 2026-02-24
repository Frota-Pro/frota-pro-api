package br.com.frotasPro.api.controller.response;

import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import br.com.frotasPro.api.domain.enums.TipoNotificacao;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificacaoResponse {

    private UUID id;
    private EventoNotificacao evento;
    private TipoNotificacao tipo;
    private String titulo;
    private String mensagem;
    private String referenciaTipo;
    private UUID referenciaId;
    private String referenciaCodigo;
    private LocalDateTime criadoEm;
    private LocalDateTime lidaEm;
    private boolean lida;
}
