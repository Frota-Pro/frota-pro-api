package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.GrupoConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GrupoContaRepository extends JpaRepository<GrupoConta, UUID> {

    @Query("""
       select g
       from GrupoConta g
       where g.codigo = :codigo
          or g.codigoExterno = :codigo
       """)
    Optional<GrupoConta> findByGrupoContaPorCodigoOuCodigoExterno(String codigo);

    Optional<GrupoConta> findByCodigo(String codigo);

    Optional<GrupoConta> findByCodigoExterno(String codigo);
}
