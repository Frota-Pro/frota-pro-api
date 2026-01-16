package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Caminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaminhaoRepository extends JpaRepository<Caminhao, UUID> {

    @Query("""
   select c
   from Caminhao c
   where (:ativo is null or c.ativo = :ativo)
     and (
       :q is null or :q = '' or
       lower(c.codigo) like lower(concat('%', :q, '%')) or
       lower(coalesce(c.codigoExterno, '')) like lower(concat('%', :q, '%')) or
       lower(coalesce(c.placa, '')) like lower(concat('%', :q, '%')) or
       lower(coalesce(c.descricao, '')) like lower(concat('%', :q, '%')) or
       lower(coalesce(c.marca, '')) like lower(concat('%', :q, '%')) or
       lower(coalesce(c.modelo, '')) like lower(concat('%', :q, '%'))
     )
""")
    Page<Caminhao> search(@Param("ativo") Boolean ativo, @Param("q") String q, Pageable pageable);


    Optional<Caminhao> findByCodigoAndAtivoTrue(String codigo);

    Optional<Caminhao> findByCodigo(String codigo);

    Optional<Caminhao> findByCodigoExterno(String codigoExterno);

    Optional<Caminhao> findByCodigoExternoAndAtivoTrue(String codigoExterno);

    Optional<Caminhao> findByPlacaAndAtivoTrue(String placa);

    @Query("""
       select c
       from Caminhao c
       where c.codigo = :codigo
          or c.codigoExterno = :codigo
       """)
    Optional<Caminhao> findByCaminhaoPorCodigoOuPorCodigoExterno(String codigo);

    List<Caminhao> findByCodigoIn(List<String> codigos);
}
