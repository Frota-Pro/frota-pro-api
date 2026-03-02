package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.CategoriaCaminhao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaCaminhaoRepository extends JpaRepository<CategoriaCaminhao, UUID> {

    Optional<CategoriaCaminhao> findByCodigo(String codigo);

    @Query("""
       select c
       from CategoriaCaminhao c
       where c.codigo = :valor
          or lower(coalesce(c.descricao, '')) = lower(:valor)
       """)
    Optional<CategoriaCaminhao> findByCodigoOuDescricao(@Param("valor") String valor);

    boolean existsByCodigo(String codigo);
}
