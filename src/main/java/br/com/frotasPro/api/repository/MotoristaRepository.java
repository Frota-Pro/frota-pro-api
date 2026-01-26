package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Motorista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MotoristaRepository extends JpaRepository<Motorista, UUID> {

    Page<Motorista> findByAtivoTrue(Pageable pageable);

    Optional<Motorista> findByCodigoAndAtivoTrue(String codigo);

    Optional<Motorista> findByCodigo(String codigo);

    Optional<Motorista> findByCodigoExterno(String codigoExterno);

    @Query("""
       select m
       from Motorista m
       where m.codigo = :codigo
          or m.codigoExterno = :codigo
       """)
    Optional<Motorista> findByMotoristaPorCodigoOuPorCodigoExterno(String codigo);

    Optional<Motorista> findByUsuarioId(UUID usuarioIdLogado);

    @Query("""
        select m
        from Motorista m
        where (:ativo is null or m.ativo = :ativo)
          and (
            :q is null or :q = '' or
            lower(m.codigo) like lower(concat('%', :q, '%')) or
            lower(coalesce(m.codigoExterno, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(m.nome, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(m.email, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(m.cnh, '')) like lower(concat('%', :q, '%'))
          )
    """)
    Page<Motorista> search(@Param("ativo") Boolean ativo, @Param("q") String q, Pageable pageable);
}
