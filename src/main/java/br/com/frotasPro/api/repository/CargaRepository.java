package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.enums.Status;
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

public interface CargaRepository extends JpaRepository<Carga, UUID> {

    Optional<Carga> findByNumeroCarga(String numero);

    Optional<Carga> findByNumeroCargaExterno(String numeroCargaExterno);

    Page<Carga> findByDtSaida(LocalDate dtSaida, Pageable pageable);

    Page<Carga> findByDtSaidaBetween(LocalDate dataInicio,
                                     LocalDate dataFim,
                                     Pageable pageable);

    Page<Carga> findByCriadoEmBetween(LocalDateTime dataHoraInicio,
                                      LocalDateTime dataHoraFim,
                                      Pageable pageable);

    @Query("""
       select c
       from Carga c
       where c.motorista.codigo = :codigo
          or c.motorista.codigoExterno = :codigo
       """)
    Page<Carga> findByMotoristaCodigoOuCodigoExterno(String codigo, Pageable pageable);

    @Query("""
       select c
       from Carga c
       where c.caminhao.codigo = :codigo
          or c.caminhao.codigoExterno = :codigo
       """)
    Page<Carga> findByCaminhaoCodigoOuCodigoExterno(String codigo, Pageable pageable);

    @Query("""
           select c
           from Carga c
           where c.motorista.codigo = :codmotorista
             and c.statusCarga in :status
           order by c.dtSaida desc
           """)
    Optional<Carga> buscarCargaAtualDoMotorista(
            @Param("codmotorista") String codmotorista,
            @Param("status") List<Status> status);

    @Query("""
            select c
            from Carga c
               join fetch c.motorista m
               join fetch c.caminhao cam
            where m.codigo = :codigoMotorista
              and c.dtSaida between :inicio and :fim
            order by c.dtSaida asc, c.id asc
            """)
    List<Carga> findByMotoristaCodigoAndPeriodo(
            @Param("codigoMotorista") String codigoMotorista,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
       select coalesce(sum(c.kmFinal - c.kmInicial), 0)
       from Carga c
       where c.caminhao.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
         and c.kmFinal is not null
         and c.kmInicial is not null
       """)
    Long sumKmRodadoPorCaminhaoNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select coalesce(sum(c.kmFinal - c.kmInicial), 0)
       from Carga c
       where c.motorista.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
         and c.kmFinal is not null
         and c.kmInicial is not null
       """)
    Long sumKmRodadoPorMotoristaNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select coalesce(sum(c.pesoCarga), 0)
       from Carga c
       where c.caminhao.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
       """)
    BigDecimal sumPesoPorCaminhaoNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select coalesce(sum(c.pesoCarga), 0)
       from Carga c
       where c.motorista.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
       """)
    BigDecimal sumPesoPorMotoristaNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select count(c)
       from Carga c
       where c.caminhao.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
       """)
    Long countCargasPorCaminhaoNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    @Query("""
       select count(c)
       from Carga c
       where c.motorista.codigo = :codigo
         and c.statusCarga = :status
         and c.dtChegada between :inicio and :fim
       """)
    Long countCargasPorMotoristaNoPeriodo(
            @Param("codigo") String codigo,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Status status
    );

    long countByStatusCarga(Status statusCarga);

    long countByStatusCargaAndDtChegada(Status statusCarga, LocalDate dtChegada);

    List<Carga> findTop5ByOrderByCriadoEmDesc();

    @Query("""
   select c
   from Carga c
   where (:q is null
          or lower(c.numeroCarga) like concat('%', lower(cast(:q as string)), '%')
          or lower(c.numeroCargaExterno) like concat('%', lower(cast(:q as string)), '%')
   )
   and (:inicio is null or c.dtSaida >= :inicio)
   and (:fim is null or c.dtSaida <= :fim)
   """)
    Page<Carga> listarFiltrado(
            @Param("q") String q,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );

    @Query("""
       select count(c)
       from Carga c
       where c.caminhao.codigo = :codigo
          or c.caminhao.codigoExterno = :codigo
       """)
    long countByCaminhaoCodigoOuCodigoExterno(@Param("codigo") String codigo);

    @Query("""
       select count(c)
       from Carga c
       where (c.caminhao.codigo = :codigo
          or c.caminhao.codigoExterno = :codigo)
         and c.statusCarga = :status
       """)
    long countByCaminhaoCodigoOuCodigoExternoAndStatus(
            @Param("codigo") String codigo,
            @Param("status") Status status
    );

    @Query("""
       select coalesce(sum(c.pesoCarga), 0)
       from Carga c
       where c.caminhao.codigo = :codigo
          or c.caminhao.codigoExterno = :codigo
       """)
    BigDecimal sumPesoPorCaminhaoCodigoOuCodigoExterno(@Param("codigo") String codigo);

    @Query("""
    select c
    from Carga c
    left join fetch c.notas n
    left join fetch c.motorista m
    left join fetch c.caminhao cam
    left join fetch c.rota r
    where c.numeroCarga = :numeroCarga
""")
    Optional<Carga> findByNumeroCargaWithNotas(@Param("numeroCarga") String numeroCarga);


    interface RankingMotoristaRow {
        String getCodigoMotorista();
        String getNomeMotorista();
        Long getTotalCargas();
        java.math.BigDecimal getTotalTonelada();
        Long getTotalKmRodado();
        java.math.BigDecimal getTotalValorCargas();
    }

    @Query("""
        select
          m.codigo as codigoMotorista,
          m.nome as nomeMotorista,
          count(c.id) as totalCargas,
          coalesce(sum(c.pesoCarga), 0) as totalTonelada,
          coalesce(sum(c.kmFinal - c.kmInicial), 0) as totalKmRodado,
          coalesce(sum(c.valorTotal), 0) as totalValorCargas
        from Carga c
        join c.motorista m
        where c.dtChegada between :inicio and :fim
        group by m.codigo, m.nome
        order by totalTonelada desc, totalValorCargas desc
    """)
    List<RankingMotoristaRow> rankingMotoristas(
            @Param("inicio") java.time.LocalDate inicio,
            @Param("fim") java.time.LocalDate fim
    );

}
