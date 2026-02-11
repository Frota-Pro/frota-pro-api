package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.ParadaCarga;
import br.com.frotasPro.api.domain.enums.TipoParada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ParadaCargaRepository extends JpaRepository<ParadaCarga, UUID> {

    Page<ParadaCarga> findByCargaNumeroCarga(String numeroCarga, Pageable pageable);

    Page<ParadaCarga> findByCargaNumeroCargaAndTipoParada(
            String numeroCarga,
            TipoParada tipoParada,
            Pageable pageable
    );

    @Query("""
           select distinct p
           from ParadaCarga p
           join p.manutencoes m
           where p.carga.numeroCarga = :numeroCarga
           """)
    Page<ParadaCarga> findParadasComManutencaoPorCarga(
            @Param("numeroCarga") String numeroCarga,
            Pageable pageable
    );

    @Query("""
    select p
    from ParadaCarga p
    where p.carga.numeroCarga = :numeroCarga
    order by p.dtFim
""")
    List<ParadaCarga> findAllByNumeroCargaOrderByDtFim(@Param("numeroCarga") String numeroCarga);

}
