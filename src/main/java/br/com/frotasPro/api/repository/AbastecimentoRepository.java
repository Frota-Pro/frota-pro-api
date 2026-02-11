package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.projections.AbastecimentoGastoPorCombustivel;
import br.com.frotasPro.api.projections.AbastecimentoResumoCaminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, UUID> {
    Optional<Abastecimento> findBycodigo(String codigo);

    @Query("""
       select a
       from Abastecimento a
       where (a.caminhao.codigo = :codigo
              or a.caminhao.codigoExterno = :codigo)
         and a.dtAbastecimento between :inicio and :fim
       order by a.dtAbastecimento desc
       """)
    Page<Abastecimento> buscarPorCodigoCaminhaoEPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );

    Page<Abastecimento> findByDtAbastecimentoBetween(
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    Page<Abastecimento> findByTipoCombustivelAndDtAbastecimentoBetween(
            TipoCombustivel tipoCombustivel,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    Page<Abastecimento> findByFormaPagamentoAndDtAbastecimentoBetween(
            FormaPagamento formaPagamento,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable
    );

    @Query("""
       select c.descricao as caminhao,
              sum(a.qtLitros) as totalLitros,
              sum(a.valorTotal) as totalValor
       from Abastecimento a
       join a.caminhao c
       where a.dtAbastecimento between :inicio and :fim
       group by c.id, c.descricao
       """)
    List<AbastecimentoResumoCaminhao> resumoPorCaminhaoNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query("""
           select a.tipoCombustivel as tipoCombustivel,
                  sum(a.qtLitros)     as totalLitros,
                  sum(a.valorTotal)   as totalValor
           from Abastecimento a
           where a.dtAbastecimento between :inicio and :fim
           group by a.tipoCombustivel
           """)
    List<AbastecimentoGastoPorCombustivel> gastoPorTipoCombustivelNoPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    Optional<Abastecimento> findFirstByCaminhaoIdOrderByDtAbastecimentoDesc(UUID caminhaoId);

    // para calcular média por odômetro de forma consistente (mesmo quando lança abastecimento fora de ordem)
    Optional<Abastecimento> findFirstByCaminhaoIdAndDtAbastecimentoLessThanOrderByDtAbastecimentoDesc(
            UUID caminhaoId,
            LocalDateTime dtAbastecimento
    );

    Optional<Abastecimento> findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndIdNotOrderByDtAbastecimentoDesc(
            UUID caminhaoId,
            LocalDateTime dtAbastecimento,
            UUID id
    );

    @Query("""
       select avg(a.mediaKmLitro)
       from Abastecimento a
       where a.caminhao.id = :caminhaoId
         and a.dtAbastecimento between :inicio and :fim
       """)
    BigDecimal mediaKmLitroPorCaminhaoEPeriodo(UUID caminhaoId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
    select a
    from Abastecimento a
    where a.caminhao.id = :caminhaoId
      and a.kmOdometro between :kmIni and :kmFim
    order by a.dtAbastecimento
""")
    List<Abastecimento> findByCaminhaoAndKmRodado(
            @Param("caminhaoId") UUID caminhaoId,
            @Param("kmIni") Integer kmInicial,
            @Param("kmFim") Integer kmFinal
    );

    @Query("""
        select coalesce(sum(a.qtLitros), 0)
        from Abastecimento a
        where a.dtAbastecimento >= :inicio
    """)
    BigDecimal sumLitrosFrom(@Param("inicio") LocalDateTime inicio);

    @Query("""
       select coalesce(sum(a.qtLitros), 0)
       from Abastecimento a
       where a.caminhao.codigo = :codigo
          or a.caminhao.codigoExterno = :codigo
       """)
    BigDecimal sumLitrosPorCaminhaoCodigoOuCodigoExterno(@Param("codigo") String codigo);

    @Query("""
       select coalesce(sum(a.valorTotal), 0)
       from Abastecimento a
       where a.caminhao.codigo = :codigo
          or a.caminhao.codigoExterno = :codigo
       """)
    BigDecimal sumValorPorCaminhaoCodigoOuCodigoExterno(@Param("codigo") String codigo);

    @Query("""
   select a
   from Abastecimento a
   where a.caminhao.codigo = :codigo
      or a.caminhao.codigoExterno = :codigo
   order by a.dtAbastecimento desc
""")
    Page<Abastecimento> buscarPorCodigoCaminhao(
            @Param("codigo") String codigo,
            Pageable pageable
    );

    @Query(
            value = """
select a.*
from tb_abastecimento a
join tb_caminhao c on c.id = a.caminhao_id
left join tb_motorista m on m.id = a.motorista_id
where (
cast(:q as text) is null
or a.codigo ilike ('%' || cast(:q as text) || '%')
or c.codigo ilike ('%' || cast(:q as text) || '%')
or coalesce(c.codigo_externo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(c.placa, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.codigo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.codigo_externo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.nome, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.posto, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.cidade, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.uf, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.numero_nota_cupom, '') ilike ('%' || cast(:q as text) || '%')
)
and (
cast(:caminhao as text) is null
or c.codigo = cast(:caminhao as text)
or c.codigo_externo = cast(:caminhao as text)
or c.placa = cast(:caminhao as text)
)
and (
cast(:motorista as text) is null
or m.codigo = cast(:motorista as text)
or m.codigo_externo = cast(:motorista as text)
)
and (cast(:tipo as text) is null or a.tipo_combustivel = cast(:tipo as text))
and (cast(:forma as text) is null or a.forma_pagamento = cast(:forma as text))


-- >>> AQUI é o que resolve o 42P18 (tipa o parâmetro mesmo quando vem null)
and (cast(:inicio as timestamp) is null or a.dt_abastecimento >= cast(:inicio as timestamp))
and (cast(:fim as timestamp) is null or a.dt_abastecimento <= cast(:fim as timestamp))


order by a.dt_abastecimento desc
""",
            countQuery = """
select count(1)
from tb_abastecimento a
join tb_caminhao c on c.id = a.caminhao_id
left join tb_motorista m on m.id = a.motorista_id
where (
cast(:q as text) is null
or a.codigo ilike ('%' || cast(:q as text) || '%')
or c.codigo ilike ('%' || cast(:q as text) || '%')
or coalesce(c.codigo_externo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(c.placa, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.codigo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.codigo_externo, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(m.nome, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.posto, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.cidade, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.uf, '') ilike ('%' || cast(:q as text) || '%')
or coalesce(a.numero_nota_cupom, '') ilike ('%' || cast(:q as text) || '%')
)
and (
cast(:caminhao as text) is null
or c.codigo = cast(:caminhao as text)
or c.codigo_externo = cast(:caminhao as text)
or c.placa = cast(:caminhao as text)
)
and (
cast(:motorista as text) is null
or m.codigo = cast(:motorista as text)
or m.codigo_externo = cast(:motorista as text)
)
and (cast(:tipo as text) is null or a.tipo_combustivel = cast(:tipo as text))
and (cast(:forma as text) is null or a.forma_pagamento = cast(:forma as text))
and (cast(:inicio as timestamp) is null or a.dt_abastecimento >= cast(:inicio as timestamp))
and (cast(:fim as timestamp) is null or a.dt_abastecimento <= cast(:fim as timestamp))
""",
            nativeQuery = true
    )
    Page<Abastecimento> filtrarNative(
            @Param("q") String q,
            @Param("caminhao") String caminhao,
            @Param("motorista") String motorista,
            @Param("tipo") String tipo,
            @Param("forma") String forma,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            Pageable pageable
    );

    @Query("""
    select a
    from Abastecimento a
    join fetch a.caminhao c
    left join fetch a.motorista m
    where a.dtAbastecimento between :inicio and :fim
      and (:caminhaoId is null or c.id = :caminhaoId)
      and (:motoristaId is null or m.id = :motoristaId)
    order by a.dtAbastecimento
""")
    List<Abastecimento> findByPeriodoComFiltro(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("caminhaoId") UUID caminhaoId,
            @Param("motoristaId") UUID motoristaId
    );


    @Query("""
    select a
    from Abastecimento a
    where a.caminhao.id = :caminhaoId
      and a.dtAbastecimento between :inicio and :fim
    order by a.dtAbastecimento
""")
    List<Abastecimento> findByCaminhaoAndPeriodo(
            @Param("caminhaoId") UUID caminhaoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
