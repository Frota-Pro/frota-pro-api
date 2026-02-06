package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Manutencao;
import br.com.frotasPro.api.domain.enums.StatusManutencao;
import br.com.frotasPro.api.projections.SerieMensalValorProjection;
import br.com.frotasPro.api.projections.TopCaminhaoCustoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query("""
        select coalesce(sum(m.valor), 0)
        from Manutencao m
        where m.oficina.codigo = :codigoOficina
          and m.dataInicioManutencao >= :inicio
          and m.dataInicioManutencao <= :fim
    """)
    BigDecimal sumValorByOficinaAndPeriodo(
            @Param("codigoOficina") String codigoOficina,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
        select count(m)
        from Manutencao m
        where m.oficina.codigo = :codigoOficina
          and m.dataInicioManutencao >= :inicio
          and m.dataInicioManutencao <= :fim
    """)
    Long countByOficinaAndPeriodo(
            @Param("codigoOficina") String codigoOficina,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
        select count(m)
        from Manutencao m
        where m.oficina.codigo = :codigoOficina
          and m.statusManutencao = :status
          and m.dataInicioManutencao >= :inicio
          and m.dataInicioManutencao <= :fim
    """)
    Long countByOficinaAndStatusAndPeriodo(
            @Param("codigoOficina") String codigoOficina,
            @Param("status") StatusManutencao status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    // Série mensal (Postgres)
    @Query(value = """
        select
          extract(year from m.data_inicio_manutencao) as ano,
          extract(month from m.data_inicio_manutencao) as mes,
          coalesce(sum(m.valor), 0) as total
        from tb_manutencao m
        join tb_oficina o on o.id = m.oficina_id
        where o.codigo = :codigoOficina
          and m.data_inicio_manutencao >= :inicio
          and m.data_inicio_manutencao <= :fim
        group by ano, mes
        order by ano, mes
    """, nativeQuery = true)
    List<SerieMensalValorProjection> serieMensalByOficina(
            @Param("codigoOficina") String codigoOficina,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    // Top caminhões por custo
    @Query(value = """
        select
          c.codigo as codigoCaminhao,
          c.descricao as descricaoCaminhao,
          coalesce(sum(m.valor), 0) as total
        from tb_manutencao m
        join tb_oficina o on o.id = m.oficina_id
        join tb_caminhao c on c.id = m.caminhao_id
        where o.codigo = :codigoOficina
          and m.data_inicio_manutencao >= :inicio
          and m.data_inicio_manutencao <= :fim
        group by c.codigo, c.descricao
        order by total desc
        limit :limit
    """, nativeQuery = true)
    List<TopCaminhaoCustoProjection> topCaminhoesByOficina(
            @Param("codigoOficina") String codigoOficina,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("limit") int limit
    );

    // Peças vs Serviços (itens detalhados)
    @Query(value = """
        select coalesce(sum(i.valor_total), 0)
        from tb_manutencao_item i
        join tb_manutencao m on m.id = i.manutencao_id
        join tb_oficina o on o.id = m.oficina_id
        where o.codigo = :codigoOficina
          and i.tipo = :tipo
          and m.data_inicio_manutencao >= :inicio
          and m.data_inicio_manutencao <= :fim
    """, nativeQuery = true)
    BigDecimal sumItensByTipoAndOficina(
            @Param("codigoOficina") String codigoOficina,
            @Param("tipo") String tipo, // "PECA" ou "SERVICO"
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

}
