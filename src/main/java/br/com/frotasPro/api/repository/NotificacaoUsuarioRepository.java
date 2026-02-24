package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.NotificacaoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificacaoUsuarioRepository extends JpaRepository<NotificacaoUsuario, UUID> {

    @EntityGraph(attributePaths = {"notificacao"})
    Page<NotificacaoUsuario> findByUsuarioIdOrderByNotificacaoCriadoEmDesc(UUID usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"notificacao"})
    Page<NotificacaoUsuario> findByUsuarioIdAndLidaEmIsNullOrderByNotificacaoCriadoEmDesc(UUID usuarioId, Pageable pageable);

    long countByUsuarioIdAndLidaEmIsNull(UUID usuarioId);

    Optional<NotificacaoUsuario> findByNotificacaoIdAndUsuarioId(UUID notificacaoId, UUID usuarioId);
}
