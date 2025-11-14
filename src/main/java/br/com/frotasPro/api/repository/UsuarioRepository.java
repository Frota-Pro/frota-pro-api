package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario,UUID> {
    Optional<Usuario> findByLogin(String login);
}
