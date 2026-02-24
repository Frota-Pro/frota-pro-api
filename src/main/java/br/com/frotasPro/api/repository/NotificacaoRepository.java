package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
}
