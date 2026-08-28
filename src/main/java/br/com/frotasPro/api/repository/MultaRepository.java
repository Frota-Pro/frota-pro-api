package br.com.frotasPro.api.repository;

import br.com.frotasPro.api.domain.Multa;
import br.com.frotasPro.api.domain.enums.StatusPagamentoMulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MultaRepository extends JpaRepository<Multa, UUID> {

    @Query("""
        select m
        from Multa m
        where (:codigoCaminhao is null or m.caminhao.codigo = :codigoCaminhao)
          and (:codigoMotorista is null or m.motorista.codigo = :codigoMotorista)
          and (:status is null or m.statusPagamento = :status)
          and (:inicio is null or m.dataInfracao >= :inicio)
          and (:fim is null or m.dataInfracao <= :fim)
        order by m.dataInfracao desc
    """)
    Page<Multa> search(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("codigoMotorista") String codigoMotorista,
            @Param("status") StatusPagamentoMulta status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );

    @Query("""
        select m
        from Multa m
        where m.notificadoPrazoEm is null
          and m.statusPagamento = br.com.frotasPro.api.domain.enums.StatusPagamentoMulta.PENDENTE
          and (
            (m.dataVencimentoPagamento is not null and m.dataVencimentoPagamento <= :limite and m.dataVencimentoPagamento >= :hoje)
            or
            (m.dataLimiteRecurso is not null and m.dataLimiteRecurso <= :limite and m.dataLimiteRecurso >= :hoje)
          )
    """)
    List<Multa> buscarComPrazoProximoNaoNotificado(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    long countByStatusPagamento(StatusPagamentoMulta status);

    @Query("select coalesce(sum(m.valor), 0) from Multa m where m.statusPagamento = :status")
    java.math.BigDecimal sumValorByStatusPagamento(@Param("status") StatusPagamentoMulta status);

    @Query("select min(m.dataVencimentoPagamento) from Multa m where m.statusPagamento = :status and m.dataVencimentoPagamento >= :hoje")
    LocalDate minVencimentoPagamento(@Param("status") StatusPagamentoMulta status, @Param("hoje") LocalDate hoje);

    @Query("select min(m.dataLimiteRecurso) from Multa m where m.statusPagamento = :status and m.dataLimiteRecurso >= :hoje")
    LocalDate minLimiteRecurso(@Param("status") StatusPagamentoMulta status, @Param("hoje") LocalDate hoje);
}
