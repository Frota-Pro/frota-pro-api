package br.com.frotasPro.api.modules.logistica.repository;

import br.com.frotasPro.api.modules.logistica.domain.MovimentacaoSemCarga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoSemCargaRepository extends JpaRepository<MovimentacaoSemCarga, UUID>, JpaSpecificationExecutor<MovimentacaoSemCarga> {

    @Query("""
       select m
       from MovimentacaoSemCarga m
       where (:codigoCaminhao is null
              or m.caminhao.codigo = :codigoCaminhao
              or m.caminhao.codigoExterno = :codigoCaminhao
              or lower(replace(coalesce(m.caminhao.placa, ''), '-', '')) = lower(replace(:codigoCaminhao, '-', '')))
         and (:inicio is null or m.dataMovimentacao >= :inicio)
         and (:fim is null or m.dataMovimentacao <= :fim)
       order by m.dataMovimentacao desc, m.criadoEm desc
       """)
    Page<MovimentacaoSemCarga> buscar(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );

    @Query("""
       select m
       from MovimentacaoSemCarga m
       where (:codigoCaminhao is null
              or m.caminhao.codigo = :codigoCaminhao
              or m.caminhao.codigoExterno = :codigoCaminhao
              or lower(replace(coalesce(m.caminhao.placa, ''), '-', '')) = lower(replace(:codigoCaminhao, '-', '')))
         and m.dataMovimentacao between :inicio and :fim
       order by m.dataMovimentacao asc, m.criadoEm asc
       """)
    List<MovimentacaoSemCarga> findByPeriodoComFiltro(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("codigoCaminhao") String codigoCaminhao
    );

    @Query("""
       select coalesce(sum(m.kmRodado), 0)
       from MovimentacaoSemCarga m
       where m.caminhao.codigo = :codigo
          or m.caminhao.codigoExterno = :codigo
       """)
    Long sumKmPorCaminhaoCodigoOuCodigoExterno(@Param("codigo") String codigo);

    @Query("""
       select coalesce(sum(m.custoEstimado), 0)
       from MovimentacaoSemCarga m
       where (:codigoCaminhao is null
              or m.caminhao.codigo = :codigoCaminhao
              or m.caminhao.codigoExterno = :codigoCaminhao
              or lower(replace(coalesce(m.caminhao.placa, ''), '-', '')) = lower(replace(:codigoCaminhao, '-', '')))
         and (:inicio is null or m.dataMovimentacao >= :inicio)
         and (:fim is null or m.dataMovimentacao <= :fim)
       """)
    BigDecimal sumCusto(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("""
       select coalesce(sum(m.kmRodado), 0)
       from MovimentacaoSemCarga m
       where (:codigoCaminhao is null
              or m.caminhao.codigo = :codigoCaminhao
              or m.caminhao.codigoExterno = :codigoCaminhao
              or lower(replace(coalesce(m.caminhao.placa, ''), '-', '')) = lower(replace(:codigoCaminhao, '-', '')))
         and (:inicio is null or m.dataMovimentacao >= :inicio)
         and (:fim is null or m.dataMovimentacao <= :fim)
       """)
    Long sumKm(
            @Param("codigoCaminhao") String codigoCaminhao,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
