package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Notificacao;
import br.com.frotasPro.api.domain.enums.EventoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
    boolean existsByEventoAndReferenciaIdAndCriadoEmAfter(EventoNotificacao evento, UUID referenciaId, LocalDateTime criadoEm);

    boolean existsByEventoAndReferenciaIdAndReferenciaCodigoAndCriadoEmAfter(
            EventoNotificacao evento,
            UUID referenciaId,
            String referenciaCodigo,
            LocalDateTime criadoEm
    );
}
