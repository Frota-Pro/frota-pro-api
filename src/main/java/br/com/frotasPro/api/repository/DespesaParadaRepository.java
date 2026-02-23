package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.DespesaParada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DespesaParadaRepository extends JpaRepository<DespesaParada, UUID> {
    @Query("""
        select d
        from DespesaParada d
        join fetch d.paradaCarga p
        join fetch p.carga c
        join fetch c.caminhao ca
        join fetch c.motorista m
        where d.dataHora between :inicio and :fim
        order by d.dataHora
    """)
    List<DespesaParada> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
