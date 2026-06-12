package br.com.frotasPro.api.modules.meta.repository;

import br.com.frotasPro.api.modules.meta.domain.MetaResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetaResultadoRepository extends JpaRepository<MetaResultado, UUID> {

    Optional<MetaResultado> findFirstByMetaIdAndCaminhaoIdAndPeriodoInicioAndPeriodoFimOrderByCalculadoEmDesc(
            UUID metaId,
            UUID caminhaoId,
            LocalDate periodoInicio,
            LocalDate periodoFim
    );

    @Query("""
        select count(distinct r.caminhao.id)
        from MetaResultado r
        where r.meta.statusMeta = br.com.frotasPro.api.domain.enums.StatusMeta.EM_ANDAMENTO
          and r.metaAtingida = false
          and r.calculadoEm = (
              select max(r2.calculadoEm)
              from MetaResultado r2
              where r2.meta = r.meta
                and r2.caminhao = r.caminhao
                and r2.periodoInicio = r.periodoInicio
                and r2.periodoFim = r.periodoFim
          )
    """)
    long countCaminhoesForaMetaAtual();

    @Query("""
        select r
        from MetaResultado r
        join fetch r.meta m
        join fetch r.caminhao c
        where m.statusMeta = br.com.frotasPro.api.domain.enums.StatusMeta.EM_ANDAMENTO
          and r.calculadoEm = (
              select max(r2.calculadoEm)
              from MetaResultado r2
              where r2.meta = r.meta
                and r2.caminhao = r.caminhao
                and r2.periodoInicio = r.periodoInicio
                and r2.periodoFim = r.periodoFim
          )
        order by r.metaAtingida desc, r.percentual asc
    """)
    List<MetaResultado> findResultadosAtuaisMetasEmAndamento();

    @Query("""
        select r
        from MetaResultado r
        join fetch r.meta m
        join fetch r.caminhao c
        where m.statusMeta = br.com.frotasPro.api.domain.enums.StatusMeta.EM_ANDAMENTO
          and r.metaAtingida = true
          and r.calculadoEm = (
              select max(r2.calculadoEm)
              from MetaResultado r2
              where r2.meta = r.meta
                and r2.caminhao = r.caminhao
                and r2.periodoInicio = r.periodoInicio
                and r2.periodoFim = r.periodoFim
          )
        order by r.percentual desc
    """)
    List<MetaResultado> findTopResultadosDentroMeta();
}
