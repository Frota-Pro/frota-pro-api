package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManutencaoRepository extends JpaRepository<Manutencao, UUID> {
    Optional<Manutencao> findByCodigo(String id);

    Page<Manutencao> findByCaminhaoCodigo(String codigoCaminhao, Pageable pageable);

    Page<Manutencao> findByCaminhaoCodigoAndDataInicioManutencaoBetween(
            String codigoCaminhao,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );

    Page<Manutencao> findByOficinaCodigoAndDataInicioManutencaoBetween(
            String codigoOficina,
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );

    Page<Manutencao> findByDataInicioManutencaoBetween(
            LocalDate inicio,
            LocalDate fim,
            Pageable pageable
    );

    List<Manutencao> findAllByCaminhaoCodigoAndDataInicioManutencaoBetween(
            String codigoCaminhao,
            LocalDate inicio,
            LocalDate fim
    );

    long countByStatusManutencaoIn(List<StatusManutencao> status);

    @Query("""
       select count(m)
       from Manutencao m
       where m.caminhao.codigo = :codigo
         and m.statusManutencao in :status
       """)
    long countAbertasPorCaminhaoCodigo(
            @Param("codigo") String codigo,
            @Param("status") List<StatusManutencao> status
    );

}
