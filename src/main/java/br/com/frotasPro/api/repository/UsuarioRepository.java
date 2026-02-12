package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario,UUID> {
    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByNome(String nome);

    boolean existsByLogin(String login);

    boolean existsByNome(String nome);

    @Query("""
            select u from Usuario u
            where (:ativo is null or u.ativo = :ativo)
              and (
                    :q is null or :q = ''
                    or lower(u.nome) like lower(concat('%', :q, '%'))
                    or lower(u.login) like lower(concat('%', :q, '%'))
                  )
            """)
    Page<Usuario> search(@Param("q") String q, @Param("ativo") Boolean ativo, Pageable pageable);
}
