package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Abastecimento;
import br.com.frotasPro.api.domain.enums.FormaPagamento;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TipoCombustivel;
import br.com.frotasPro.api.projections.AbastecimentoGastoPorCombustivel;
import br.com.frotasPro.api.projections.AbastecimentoResumoCaminhao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /** Base da página de Analytics — todos os abastecimentos do período, pra série de tendência semanal. */
    List<Abastecimento> findAllByDtAbastecimentoBetween(LocalDateTime inicio, LocalDateTime fim);

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
              sum(a.valorTotal) as totalValor,
              case
                when sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end) = 0 then null
                else sum(a.mediaKmLitro * coalesce(a.qtLitros, 0))
                     / sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end)
              end as mediaKmLitro
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

    Optional<Abastecimento> findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndKmOdometroIsNotNullOrderByDtAbastecimentoDesc(
            UUID caminhaoId,
            LocalDateTime dtAbastecimento
    );

    Optional<Abastecimento> findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndIdNotOrderByDtAbastecimentoDesc(
            UUID caminhaoId,
            LocalDateTime dtAbastecimento,
            UUID id
    );

    Optional<Abastecimento> findFirstByCaminhaoIdAndDtAbastecimentoLessThanAndIdNotAndKmOdometroIsNotNullOrderByDtAbastecimentoDesc(
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
       select case
                when sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end) = 0 then null
                else sum(a.mediaKmLitro * coalesce(a.qtLitros, 0))
                     / sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end)
              end
       from Abastecimento a
       where a.caminhao.id = :caminhaoId
         and a.dtAbastecimento between :inicio and :fim
       """)
    BigDecimal mediaKmLitroPonderadaPorCaminhaoEPeriodo(UUID caminhaoId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
       select case
                when sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end) = 0 then null
                else sum(a.mediaKmLitro * coalesce(a.qtLitros, 0))
                     / sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end)
              end
       from Abastecimento a
       where a.caminhao.id = :caminhaoId
         and a.mediaKmLitro is not null
       """)
    BigDecimal mediaKmLitroPonderadaPorCaminhao(UUID caminhaoId);

    @Query("""
       select case
                when coalesce(sum(a.qtLitros), 0) = 0 then null
                else sum(coalesce(a.valorTotal, 0)) / sum(a.qtLitros)
              end
       from Abastecimento a
       where a.caminhao.id = :caminhaoId
       """)
    BigDecimal valorLitroMedioPorCaminhao(UUID caminhaoId);

    @Query("""
       select case
                when sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end) = 0 then null
                else sum(a.mediaKmLitro * coalesce(a.qtLitros, 0))
                     / sum(case when a.mediaKmLitro is not null then coalesce(a.qtLitros, 0) else 0 end)
              end
       from Abastecimento a
       where a.motorista.id = :motoristaId
         and a.dtAbastecimento between :inicio and :fim
       """)
    BigDecimal mediaKmLitroPonderadaPorMotoristaEPeriodo(UUID motoristaId, LocalDateTime inicio, LocalDateTime fim);

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
or c.codigo ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.codigo_externo, '') ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.placa, '') ilike ('%' || cast(:caminhao as text) || '%')
)
and (
cast(:motorista as text) is null
or coalesce(m.codigo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.codigo_externo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.nome, '') ilike ('%' || cast(:motorista as text) || '%')
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
or c.codigo ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.codigo_externo, '') ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.placa, '') ilike ('%' || cast(:caminhao as text) || '%')
)
and (
cast(:motorista as text) is null
or coalesce(m.codigo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.codigo_externo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.nome, '') ilike ('%' || cast(:motorista as text) || '%')
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

    @Query("""
       select a
       from Abastecimento a
       join a.paradaCarga p
       where p.carga.id = :cargaId
       order by a.dtAbastecimento
       """)
    List<Abastecimento> findByCargaId(@Param("cargaId") UUID cargaId);

    // Litros abastecidos DURANTE cargas finalizadas cujo início (dtSaida) caiu
    // no período — não filtra pela data do abastecimento em si, porque um
    // abastecimento pode acontecer já no mês seguinte ao início da carga
    // (a carga é que decide o período, não o abastecimento isolado).
    @Query("""
       select coalesce(sum(a.qtLitros), 0)
       from Abastecimento a
       join a.paradaCarga p
       join p.carga c
       where c.caminhao.codigo = :codigo
         and c.statusCarga = :status
         and c.dtSaida between :inicio and :fim
       """)
    BigDecimal sumLitrosVinculadosACargaPorCaminhaoNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select coalesce(sum(a.qtLitros), 0)
       from Abastecimento a
       join a.paradaCarga p
       join p.carga c
       where c.motorista.codigo = :codigo
         and c.statusCarga = :status
         and c.dtSaida between :inicio and :fim
       """)
    BigDecimal sumLitrosVinculadosACargaPorMotoristaNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    boolean existsByPostoAbastecimento_Id(UUID id);

    /** Analytics por motorista/caminhão — abastecimentos de um específico no período. */
    List<Abastecimento> findAllByMotorista_CodigoAndDtAbastecimentoBetween(String codigoMotorista, LocalDateTime inicio, LocalDateTime fim);

    List<Abastecimento> findAllByCaminhao_CodigoAndDtAbastecimentoBetween(String codigoCaminhao, LocalDateTime inicio, LocalDateTime fim);

    /** Analytics de abastecimento — total por posto (cadastrado ou texto livre) no período. */
    @Query("""
       select coalesce(pa.nome, a.posto, 'Não informado') as posto,
              sum(a.qtLitros) as totalLitros,
              sum(a.valorTotal) as totalValor
       from Abastecimento a
       left join a.postoAbastecimento pa
       where a.dtAbastecimento between :inicio and :fim
       group by coalesce(pa.nome, a.posto, 'Não informado')
       order by sum(a.valorTotal) desc
       """)
    List<ResumoPostoRow> resumoPorPostoNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    interface ResumoPostoRow {
        String getPosto();
        BigDecimal getTotalLitros();
        BigDecimal getTotalValor();
    }

    /**
     * Agregado da tela de Abastecimentos — usa EXATAMENTE o mesmo filtro de
     * {@link #filtrarNative}, mas soma tudo que bate com o filtro (todas as
     * páginas), não só a página carregada. Os cards da tela (litros, gasto
     * total, preço médio, consumo médio) dependem disso pra não ficarem
     * errados quando a lista tem mais de uma página.
     */
    @Query(
            value = """
select
  coalesce(sum(a.qt_litros), 0) as totalLitros,
  coalesce(sum(a.valor_total), 0) as totalValor,
  coalesce(sum(case when a.media_km_litro is not null and a.qt_litros is not null then a.media_km_litro * a.qt_litros else 0 end), 0) as somaMediaPonderada,
  coalesce(sum(case when a.media_km_litro is not null and a.qt_litros is not null then a.qt_litros else 0 end), 0) as somaLitrosParaMedia,
  count(1) as totalRegistros
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
or c.codigo ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.codigo_externo, '') ilike ('%' || cast(:caminhao as text) || '%')
or coalesce(c.placa, '') ilike ('%' || cast(:caminhao as text) || '%')
)
and (
cast(:motorista as text) is null
or coalesce(m.codigo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.codigo_externo, '') ilike ('%' || cast(:motorista as text) || '%')
or coalesce(m.nome, '') ilike ('%' || cast(:motorista as text) || '%')
)
and (cast(:tipo as text) is null or a.tipo_combustivel = cast(:tipo as text))
and (cast(:forma as text) is null or a.forma_pagamento = cast(:forma as text))
and (cast(:inicio as timestamp) is null or a.dt_abastecimento >= cast(:inicio as timestamp))
and (cast(:fim as timestamp) is null or a.dt_abastecimento <= cast(:fim as timestamp))
""",
            nativeQuery = true
    )
    ResumoFiltroRow resumoFiltradoNative(
            @Param("q") String q,
            @Param("caminhao") String caminhao,
            @Param("motorista") String motorista,
            @Param("tipo") String tipo,
            @Param("forma") String forma,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    interface ResumoFiltroRow {
        BigDecimal getTotalLitros();
        BigDecimal getTotalValor();
        BigDecimal getSomaMediaPonderada();
        BigDecimal getSomaLitrosParaMedia();
        long getTotalRegistros();
    }

    /**
     * Preço médio/L recente (mesmo posto + tipo de combustível, últimos N dias
     * antes desse abastecimento) — base pra {@code DetectarAnomaliaAbastecimentoService}.
     * Casa por posto cadastrado quando o abastecimento tem um vinculado; senão
     * casa pelo texto livre do campo "posto" (mesmo abastecimento nunca conta
     * na própria média, via :excluirId).
     */
    @Query(
            value = """
select
  avg(a.valor_litro) as mediaPreco,
  count(1) as amostras
from tb_abastecimento a
where a.tipo_combustivel = cast(:tipoCombustivel as text)
  and a.valor_litro is not null
  and a.dt_abastecimento >= cast(:desde as timestamp)
  and a.dt_abastecimento < cast(:ate as timestamp)
  and (cast(:excluirId as uuid) is null or a.id <> cast(:excluirId as uuid))
  and (
    (cast(:postoAbastecimentoId as uuid) is not null and a.posto_abastecimento_id = cast(:postoAbastecimentoId as uuid))
    or (cast(:postoAbastecimentoId as uuid) is null and cast(:posto as text) is not null and lower(coalesce(a.posto, '')) = lower(cast(:posto as text)))
  )
""",
            nativeQuery = true
    )
    ReferenciaPrecoRow referenciaPrecoPostoCombustivel(
            @Param("tipoCombustivel") String tipoCombustivel,
            @Param("desde") LocalDateTime desde,
            @Param("ate") LocalDateTime ate,
            @Param("excluirId") UUID excluirId,
            @Param("postoAbastecimentoId") UUID postoAbastecimentoId,
            @Param("posto") String posto
    );

    interface ReferenciaPrecoRow {
        BigDecimal getMediaPreco();
        Long getAmostras();
    }
}
