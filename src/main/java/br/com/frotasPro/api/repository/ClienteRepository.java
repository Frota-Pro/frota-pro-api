package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByDocumento(String documento);

    @Query("""
        select c
        from Cliente c
        where (
            :q is null or :q = '' or
            lower(c.nome) like lower(concat('%', :q, '%')) or
            lower(c.documento) like lower(concat('%', :q, '%')) or
            lower(coalesce(c.cidade, '')) like lower(concat('%', :q, '%'))
        )
    """)
    Page<Cliente> search(@Param("q") String q, Pageable pageable);
}
