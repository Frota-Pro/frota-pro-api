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
}