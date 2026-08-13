package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.PostoAbastecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostoAbastecimentoRepository extends JpaRepository<PostoAbastecimento, UUID> {

    Optional<PostoAbastecimento> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<PostoAbastecimento> findAllByAtivoTrueOrderByNomeAsc();

    @Query("""
       select p
       from PostoAbastecimento p
       where (:ativo is null or p.ativo = :ativo)
         and (
           :q is null or :q = '' or
           lower(p.codigo) like lower(concat('%', :q, '%')) or
           lower(p.nome) like lower(concat('%', :q, '%')) or
           lower(coalesce(p.cidade, '')) like lower(concat('%', :q, '%')) or
           lower(coalesce(p.cnpj, '')) like lower(concat('%', :q, '%'))
         )
       """)
    Page<PostoAbastecimento> search(@Param("ativo") Boolean ativo, @Param("q") String q, Pageable pageable);
}
