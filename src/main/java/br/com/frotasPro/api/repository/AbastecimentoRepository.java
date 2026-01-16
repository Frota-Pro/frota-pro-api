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



}
